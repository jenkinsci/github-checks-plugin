package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;

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
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.ParameterizedJobMixIn;

import io.jenkins.plugins.checks.github.status.GitHubStatusChecksProperties;

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
    private final GitHubStatusChecksProperties githubStatusChecksProperties;

    /**
     * Construct the subscriber.
     */
    public CheckRunGHEventSubscriber() {
        this(new JenkinsFacade(), new SCMFacade(), new GitHubStatusChecksProperties());
    }

    @VisibleForTesting
    CheckRunGHEventSubscriber(final JenkinsFacade jenkinsFacade, final SCMFacade scmFacade, 
                              final GitHubStatusChecksProperties githubStatusChecksProperties) {
        super();

        this.jenkinsFacade = jenkinsFacade;
        this.scmFacade = scmFacade;
        this.githubStatusChecksProperties = githubStatusChecksProperties;
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
        return Set.copyOf(Collections.singletonList(GHEvent.CHECK_RUN));
    }

    @Override
    protected void onEvent(final GHSubscriberEvent event) {
        final String payload = event.getPayload();
        try {
            GHEventPayload.CheckRun checkRun = GitHub.offline().parseEventPayload(new StringReader(payload), GHEventPayload.CheckRun.class);
            if (!RERUN_ACTION.equals(checkRun.getAction())) {
                LOGGER.log(Level.FINE,
                        "Unsupported check run action: " + checkRun.getAction().replaceAll("[\r\n]", ""));
                return;
            }

            JSONObject payloadJSON = new JSONObject(payload);

            LOGGER.log(Level.INFO, "Received rerun request through GitHub checks API.");
            try (ACLContext ignored = ACL.as2(ACL.SYSTEM2)) {
                String branchName = payloadJSON.getJSONObject("check_run").getJSONObject("check_suite").optString("head_branch");
                final GHRepository repository = checkRun.getRepository();

                Optional<Run<?, ?>> optionalRun = jenkinsFacade.getBuild(checkRun.getCheckRun().getExternalId());
                if (optionalRun.isPresent()) {
                    Run<?, ?> run = optionalRun.get();
                    Job<?, ?> job = run.getParent();
                    boolean isDisableRerunAction = githubStatusChecksProperties.isDisableRerunAction(job);
                    String rerunActionRole = githubStatusChecksProperties.getRerunActionRole(job);

                    if (!isDisableRerunAction) {
                        if (!rerunActionRole.isBlank()) {
                            User user = User.get(checkRun.getSender().getLogin());
                            List<String> userRoles = user.getAuthorities();
                            if (userRoles.contains(rerunActionRole)) {
                                scheduleRerun(checkRun, branchName, run, job);
                            } else {
                                LOGGER.log(
                                    Level.WARNING, 
                                    String.format(
                                        "The user %s does not have the required %s role for the rerun action on job %s", 
                                        checkRun.getSender().getLogin(), 
                                        rerunActionRole,
                                        jenkinsFacade.getFullNameOf(job)
                                    )
                                );
                            }
                        } else {
                            scheduleRerun(checkRun, branchName, run, job);
                        }
                    } else {
                        LOGGER.log(Level.INFO, String.format("Rerun action is disabled for job %s", jenkinsFacade.getFullNameOf(job)));
                    }
                }
                else {
                    LOGGER.log(Level.WARNING, String.format("No build found for rerun request from repository: %s and id: %s",
                            repository.getFullName(), checkRun.getCheckRun().getExternalId()).replaceAll("[\r\n]", ""));
                }
            }
        }
        catch (IOException | JSONException e) {
            throw new IllegalStateException("Could not parse check run event: " + payload.replaceAll("[\r\n]", ""), e);
        }
    }

    private void scheduleRerun(final GHEventPayload.CheckRun checkRun, final String branchName, final Run<?, ?> run, final Job<?, ?> job) {
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
