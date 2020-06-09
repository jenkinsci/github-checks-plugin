package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;
import org.kohsuke.github.GitHubBuilder;

import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import hudson.Extension;

import io.jenkins.plugins.github.checks.api.ChecksDetails;

/*
 * A Client used to connect GitHub and manipulate check runs.
 */
@Extension
public class GitHubChecksClient extends ChecksClient {
    private static final Logger LOGGER = Logger.getLogger(GitHubChecksClient.class.getName());

    @Override
    public boolean isApplicable(final ChecksContext context) {
        return (context.getSource() instanceof GitHubSCMSource);
    }

    /**
     * Create a check with <code>details</code> of a specific repository and commit described in <code>details</code>.
     *
     * @param context
     *         context of the check
     * @param details
     *         details of the check
     */
    public void createCheckRun(ChecksContext context, ChecksDetails details) {
        String repository = context.getRepository();
        String headSha = context.getHeadSha();
        String token = context.getToken();

        if (StringUtils.isNoneBlank(repository, headSha, token)) {
            try {
                new GitHubBuilder().withAppInstallationToken(token).build()
                        .getRepository(repository).createCheckRun(details.getName(), headSha)
                        .withStartedAt(new Date())
                        .withDetailsURL(StringUtils.defaultIfBlank(details.getDetailsURL(), context.getRun().getUrl()))
                        .withStatus(Status.QUEUED)
                        .create();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not create check run. Message: ", e);
            }
        }
    }

    /**
     * Complete a check with <code>details</code> of a specific repository and commit described in <code>details</code>.
     *
     * @param context
     *         context of the check
     * @param details
     *         details of the check
     */
    public void updateCheckRun(ChecksContext context, ChecksDetails details) {
        String repository = context.getRepository();
        String headSha = context.getHeadSha();
        String token = context.getToken();

        if (StringUtils.isNoneBlank(repository, headSha, token)) {
            try {
                new GitHubBuilder().withAppInstallationToken(token).build()
                        .getRepository(repository).createCheckRun(details.getName(), headSha)
                        .withStartedAt(new Date())
                        .withDetailsURL(StringUtils.defaultIfBlank(details.getDetailsURL(), context.getRun().getUrl()))
                        .withStatus(Status.IN_PROGRESS)
                        .create();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not update check run. Message: ", e);
            }
        }
    }

    /**
     * Complete a check with <code>details</code> of a specific repository and commit described in <code>details</code>.
     *
     * @param context
     *         context of the check
     * @param details
     *         details of the check
     */
    public void completeCheckRun(ChecksContext context, ChecksDetails details) {
        String repository = context.getRepository();
        String headSha = context.getHeadSha();
        String token = context.getToken();

        if (StringUtils.isNoneBlank(repository, headSha, token)) {
            try {
                new GitHubBuilder().withAppInstallationToken(token).build()
                        .getRepository(repository).createCheckRun(details.getName(), headSha)
                        .withCompletedAt(new Date())
                        .withDetailsURL(StringUtils.defaultIfBlank(details.getDetailsURL(), context.getRun().getUrl()))
                        .withStatus(Status.COMPLETED)
                        .withConclusion(Conclusion.SUCCESS)
                        .create();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not complete check run. Message: ", e);
            }
        }
    }
}
