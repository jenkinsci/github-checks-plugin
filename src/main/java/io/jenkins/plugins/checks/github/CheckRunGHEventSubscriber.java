package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import hudson.security.ACLContext;
import io.jenkins.plugins.util.JenkinsFacade;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.plugins.github.extension.GHEventsSubscriber;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.kohsuke.github.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This subscriber manages {@link GHEvent#CHECK_RUN} event and handles the re-run action request.
 */
@Extension
public class CheckRunGHEventSubscriber extends GHEventsSubscriber {
    private static final Logger LOGGER = Logger.getLogger(CheckRunGHEventSubscriber.class.getName());

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
    protected boolean isApplicable(@Nullable final Item item) {
        if (item instanceof Job<?, ?>) {
            return scmFacade.findGitHubSCMSource((Job<?, ?>)item).isPresent();
        }

        return false;
    }

    @Override
    protected Set<GHEvent> events() {
        return Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(GHEvent.CHECK_RUN)));
    }

    @Override
    protected void onEvent(final GHSubscriberEvent event) {
        final String payload = event.getPayload();
        GHEventPayload.CheckRun checkRun;
        try {
            checkRun = GitHub.offline().parseEventPayload(new StringReader(payload), GHEventPayload.CheckRun.class);
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not parse check run event: " + payload.replaceAll("[\r\n]", ""), e);
        }

        if (!checkRun.getAction().equals("rerequested")) {
            LOGGER.log(Level.FINE, "Unsupported check run action: " + checkRun.getAction().replaceAll("[\r\n]", ""));
            return;
        }

        LOGGER.log(Level.INFO, "Received rerun request through GitHub checks API.");
        try (ACLContext ignored = ACL.as(ACL.SYSTEM)) {
            scheduleRerun(checkRun, payload);
        }
    }

    private void scheduleRerun(final GHEventPayload.CheckRun checkRun, final String payload) {
        final GHRepository repository = checkRun.getRepository();
        final String branchName = getBranchName(checkRun, payload);

        for (Job<?, ?> job : jenkinsFacade.getAllJobs()) {
            Optional<GitHubSCMSource> source = scmFacade.findGitHubSCMSource(job);

            if (source.isPresent() && source.get().getRepoOwner().equals(repository.getOwnerName())
                    && source.get().getRepository().equals(repository.getName())
                    && job.getName().equals(branchName)) {
                Cause cause = new GitHubChecksRerunActionCause(checkRun.getSender().getLogin());
                ParameterizedJobMixIn.scheduleBuild2(job, 0, new CauseAction(cause));

                LOGGER.log(Level.INFO, String.format("Scheduled rerun (build #%d) for job %s, requested by %s",
                        job.getNextBuildNumber(), jenkinsFacade.getFullNameOf(job),
                        checkRun.getSender().getLogin()).replaceAll("[\r\n]", ""));
                return;
            }
        }

        LOGGER.log(Level.WARNING, String.format("No proper job found for the rerun request from repository: %s and "
                + "branch: %s", repository.getFullName(), branchName).replaceAll("[\r\n]", ""));
    }

    private String getBranchName(final GHEventPayload.CheckRun checkRun, final String payload) {
        String branchName = "master";
        try {
            List<GHPullRequest> pullRequests = checkRun.getCheckRun().getPullRequests();
            if (!pullRequests.isEmpty()) {
                branchName = "PR-" + pullRequests.get(0).getNumber();
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not get pull request participated in rerun request: "
                    + payload.replaceAll("\r\n", ""), e);
        }

        return branchName;
    }

    /**
     * Declares that a build was started due to a user's rerun request through GitHub checks API.
     */
    public static class GitHubChecksRerunActionCause extends Cause {
        private final String user;

        /**
         * Construct the cause with user who requested the rerun.
         *
         * @param user
         *         name of the user who made the request
         */
        public GitHubChecksRerunActionCause(final String user) {
            super();

            this.user = user;
        }

        @Override
        public String getShortDescription() {
            return String.format("Rerun request by %s through GitHub checks API", user);
        }
    }
}
