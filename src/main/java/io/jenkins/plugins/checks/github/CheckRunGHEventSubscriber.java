package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import hudson.security.ACLContext;
import io.jenkins.plugins.util.JenkinsFacade;
import jenkins.model.ParameterizedJobMixIn;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github.extension.GHEventsSubscriber;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.kohsuke.github.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This subscriber manages {@link GHEvent#CHECK_RUN} event and handles the re-run action request.
 */
@Extension
public class CheckRunGHEventSubscriber extends GHEventsSubscriber {
    private static final Logger LOGGER = Logger.getLogger(CheckRunGHEventSubscriber.class.getName());
    private static final Pattern REPOSITORY_URL_PATTERN = Pattern.compile("https?://([^/]+)/([^/]+)/([^/]+)");

    private final JenkinsFacade jenkinsFacade;
    private final GitHubSCMFacade gitHubSCMFacade;

    /**
     * Construct the subscriber.
     */
    public CheckRunGHEventSubscriber() {
        this(new JenkinsFacade(), new GitHubSCMFacade());
    }

    @VisibleForTesting
    CheckRunGHEventSubscriber(final JenkinsFacade jenkinsFacade, final GitHubSCMFacade gitHubSCMFacade) {
        super();

        this.jenkinsFacade = jenkinsFacade;
        this.gitHubSCMFacade = gitHubSCMFacade;
    }

    @Override
    protected boolean isApplicable(@Nullable final Item item) {
        if (item instanceof Job<?, ?>) {
            return gitHubSCMFacade.findGitHubSCMSource((Job<?, ?>)item).isPresent();
        }

        return false;
    }

    @Override
    protected Set<GHEvent> events() {
        return Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(GHEvent.CHECK_RUN)));
    }

    @Override
    protected void onEvent(final GHSubscriberEvent event) {
         // TODO: open a PR in Checks API to expose properties in GHRequestedAction
        final String payload = event.getPayload();
        JSONObject json = JSONObject.fromObject(payload);
        if (!json.getString("action").equals("requested_action")
                || !json.getJSONObject("requested_action").get("identifier").equals("rerun")) {
            LOGGER.log(Level.FINE, "Unsupported check run event: " + payload.replaceAll("[\r\n]", ""));
            return;
        }

        LOGGER.log(Level.INFO, "Received rerun request through GitHub checks API.");

        GHEventPayload.CheckRun checkRun;
        try {
            checkRun = GitHub.offline().parseEventPayload(new StringReader(payload), GHEventPayload.CheckRun.class);
        }
        catch (IOException e) {
            throw new IllegalStateException("Received malformed rerun request: " + payload.replaceAll("\r\n", ""), e);
        }

        try (ACLContext ignored = ACL.as(ACL.SYSTEM)) {
            scheduleRerun(checkRun, payload);
        }
    }

    private GHRepository getRepository(final GHEventPayload.CheckRun checkRun) {
        final GHRepository repository = checkRun.getRepository();
        final String repoUrl = repository.getHtmlUrl().toExternalForm();
        if (!REPOSITORY_URL_PATTERN.matcher(repoUrl).matches()) {
            throw new IllegalStateException("Malformed repository URL in rerun request: "
                    + repoUrl.replaceAll("[\r\n]", ""));
        }

        return repository;
    }

    /**
     * Get branch name from {@link JSONObject}.
     *
     * This method will be replaced by {@link CheckRunGHEventSubscriber#getBranchName(GHEventPayload.CheckRun, String)}
     * after the release of github--api-plugin 1.116. The github-api has already released 1.116 and make
     * {@code getPullRequests} method public, see https://github.com/hub4j/github-api/pull/909.
     *
     * @param json
     *         json object from check run payload
     * @return name of the branch to be scheduled
     */
    private String getBranchName(final JSONObject json) {
        String branchName = "master";
        JSONArray pullRequests = json.getJSONObject("check_run").getJSONArray("pull_requests");
        if (!pullRequests.isEmpty()) {
            branchName = "PR-" + pullRequests.getJSONObject(0).getString("number");
        }

        return branchName;
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
    private String getBranchName(final GHEventPayload.CheckRun checkRun, final String payload) {
//        String branchName = "master";
//        try {
//            List<GHPullRequest> pullRequests = checkRun.getCheckRun().getPullRequests();
//            if (!pullRequests.isEmpty()) {
//                branchName = "PR" + pullRequests.get(0).getNumber();
//            }
//        }
//        catch (IOException e) {
//            throw new IllegalStateException("Could not get pull request participated in rerun request: "
//                    + payload.replaceAll("\r\n", ""), e);
//        }
//
//        return branchName;

        return StringUtils.EMPTY;
    }

    @VisibleForTesting
    void scheduleRerun(final GHEventPayload.CheckRun checkRun, final String payload) {
        final GHRepository repository = getRepository(checkRun);
        final String branchName = getBranchName(JSONObject.fromObject(payload));

        for (Job<?, ?> job : jenkinsFacade.getAllJobs()) {
            Optional<GitHubSCMSource> source = gitHubSCMFacade.findGitHubSCMSource(job);

            if (source.isPresent() && source.get().getRepoOwner().equals(repository.getOwnerName())
                    && source.get().getRepository().equals(repository.getName())
                    && job.getName().equals(branchName)) {
                Cause cause = new GitHubChecksRerunActionCause(checkRun.getSender().getLogin());
                ParameterizedJobMixIn.scheduleBuild2(job, 0, new CauseAction(cause));

                LOGGER.log(Level.INFO, String.format("Scheduled rerun (build #%d) for job %s,requested by %s",
                        job.getNextBuildNumber(), job.getParent().getDisplayName() + "/" + job.getName(),
                        checkRun.getSender().getLogin())
                        .replaceAll("[\r\n]", ""));

                break;
            }
        }
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
