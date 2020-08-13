package io.jenkins.plugins.checks.github;

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import io.jenkins.plugins.util.JenkinsFacade;
import jenkins.model.ParameterizedJobMixIn;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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

    @Override
    protected boolean isApplicable(final @Nullable Item item) {
        if (item instanceof Job<?, ?>) {
            Job<?, ?> job = (Job<?, ?>)item;
            return new GitHubSCMFacade().findGitHubSCMSource(job).isPresent();
        }

        return false;
    }

    @Override
    protected Set<GHEvent> events() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(GHEvent.CHECK_RUN)));
    }

    @Override
    protected void onEvent(final GHSubscriberEvent event) {
         // TODO: open a PR in Checks API to expose properties in GHRequestedAction
        JSONObject json = JSONObject.fromObject(event.getPayload());
        if (!json.getString("action").equals("requested_action")
                || !json.getJSONObject("requested_action").get("identifier").equals("rerun")) {
            return;
        }

        GHEventPayload.CheckRun payload = null;
        try {
            payload = GitHub.offline().parseEventPayload(new StringReader(event.getPayload()), GHEventPayload.CheckRun.class);
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING,
                    ("Received malformed CheckRun event: " + event.getPayload()).replaceAll("[\r\n]", ""), e);
            return;
        }

        final GHRepository repository = payload.getRepository();
        final String repoUrl = repository.getHtmlUrl().toExternalForm();
        if (!REPOSITORY_URL_PATTERN.matcher(repoUrl).matches()) {
            LOGGER.log(Level.WARNING, ("Malformed repository URL: " + repoUrl).replaceAll("[\r\n]", ""));
            return;
        }

        ACL.impersonate(ACL.SYSTEM, () -> {
            final JSONArray pullRequests = json.getJSONObject("check_run").getJSONArray("pull_requests");
            String branchName = "master";
            if (pullRequests.size() != 0) {
                branchName = "PR-" + pullRequests.getJSONObject(0).get("number");
            }

            for (Job<?, ?> job : new JenkinsFacade().getAllJobs()) {
                Optional<GitHubSCMSource> source = new GitHubSCMFacade().findGitHubSCMSource(job);

                if (source.isPresent() && source.get().getRepoOwner().equals(repository.getOwnerName())
                        && source.get().getRepository().equals(repository.getName())
                        && job.getName().equals(branchName)) {
                    ParameterizedJobMixIn.scheduleBuild2(job, 0,
                            new CauseAction(new GitHubChecksRerunActionCause()));
                    break;
                }
            }
        });
    }

    /**
     * Declares that a build was started due to a user's rerun request through GitHub checks API.
     */
    public static class GitHubChecksRerunActionCause extends Cause {
        @Override
        public String getShortDescription() {
            return "GitHub checks rerun action";
        }
    }
}
