package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.jenkinsci.plugins.github.extension.GHEventsSubscriber;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.ParameterizedJobMixIn;

import io.jenkins.plugins.util.JenkinsFacade;

/**
 * This subscriber manages {@link GHEvent#CHECK_RUN} event and handles the re-run action request.
 */
@Extension
public class CheckRunGHEventSubscriber extends GHEventsSubscriber {
    private static final Logger LOGGER = Logger.getLogger(CheckRunGHEventSubscriber.class.getName());
    private static final String RERUN_ACTION = "rerequested";

    private final JenkinsFacade jenkinsFacade;
    private final SCMFacade scmFacade;

    /**
     * Construct the subscriber.
     */
    public CheckRunGHEventSubscriber() {
        this(new JenkinsFacade(), new SCMFacade());
    }

    @VisibleForTesting
    CheckRunGHEventSubscriber(final JenkinsFacade jenkinsFacade, final SCMFacade scmFacade) {
        super();

        this.jenkinsFacade = jenkinsFacade;
        this.scmFacade = scmFacade;
    }

    @Override
    protected boolean isApplicable(@CheckForNull final Item item) {
        if (item instanceof Job<?, ?>) {
            return scmFacade.findGitHubSCMSource((Job<?, ?>) item).isPresent();
        }

        return false;
    }

    @Override
    protected Set<GHEvent> events() {
        return Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(GHEvent.CHECK_RUN)));
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "Return value of parseEventPayload method is safe to cast.")
    protected void onEvent(final GHSubscriberEvent event) {
        final String payload = event.getPayload();
        try {
            GHEventPayload.CheckRun checkRun = GitHub.offline().parseEventPayload(new StringReader(payload), GHEventPayload.CheckRun.class);
            JSONObject payloadJSON = new JSONObject(payload);

            if (!RERUN_ACTION.equals(checkRun.getAction())) {
                LOGGER.log(Level.FINE,
                        "Unsupported check run action: " + checkRun.getAction().replaceAll("[\r\n]", ""));
                return;
            }

            LOGGER.log(Level.INFO, "Received rerun request through GitHub checks API.");
            try (ACLContext ignored = ACL.as(ACL.SYSTEM)) {
                String branchName = payloadJSON.getJSONObject("check_run").getJSONObject("check_suite").optString("head_branch");
                scheduleRerun(checkRun, branchName);
            }
        }
        catch (IOException | JSONException e) {
            throw new IllegalStateException("Could not parse check run event: " + payload.replaceAll("[\r\n]", ""), e);
        }
    }

    private void scheduleRerun(final GHEventPayload.CheckRun checkRun, final String branchName) {
        final GHRepository repository = checkRun.getRepository();

        Optional<Run<?, ?>> optionalRun = jenkinsFacade.getBuild(checkRun.getCheckRun().getExternalId());
        if (optionalRun.isPresent()) {
            Run<?, ?> run = optionalRun.get();
            Job<?, ?> job = run.getParent();

            Cause cause = new GitHubChecksRerunActionCause(checkRun.getSender().getLogin(), branchName);

            List<Action> actions = new ArrayList<>();
            actions.add(new CauseAction(cause));

            ParametersAction paramAction = run.getAction(ParametersAction.class);
            if (paramAction != null) {
                actions.add(paramAction);
            }

            ParameterizedJobMixIn.scheduleBuild2(job, 0, actions.toArray(new Action[0]));

            LOGGER.log(Level.INFO, String.format("Scheduled rerun (build #%d) for job %s, requested by %s",
                    job.getNextBuildNumber(), jenkinsFacade.getFullNameOf(job),
                    checkRun.getSender().getLogin()).replaceAll("[\r\n]", ""));
        }
        else {
            LOGGER.log(Level.WARNING, String.format("No build found for rerun request from repository: %s and id: %s",
                    repository.getFullName(), checkRun.getCheckRun().getExternalId()).replaceAll("[\r\n]", ""));
        }
    }

    /**
     * Declares that a build was started due to a user's rerun request through GitHub checks API.
     */
    public static class GitHubChecksRerunActionCause extends Cause {
        private final String user;
        private final String branchName;

        /**
         * Construct the cause with user who requested the rerun.
         *
         * @param user
         *         name of the user who made the request
         * @param branchName
         *         name of the branch for which checks are to be run against
         */
        public GitHubChecksRerunActionCause(final String user, final String branchName) {
            super();

            this.user = user;
            this.branchName = branchName;
        }

        public String getBranchName() {
            return this.branchName;
        }

        @Override
        public String getShortDescription() {
            return String.format("Rerun request by %s through GitHub checks API, for branch %s", user, branchName);
        }
    }
}
