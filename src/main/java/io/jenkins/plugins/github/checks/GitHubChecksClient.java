package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;
import org.kohsuke.github.GitHubBuilder;

import io.jenkins.plugins.github.checks.api.ChecksDetails;

/*
 * A Client used to connect GitHub and manipulate check runs.
 */
public class GitHubChecksClient {
    private static final Logger LOGGER = Logger.getLogger(GitHubChecksClient.class.getName());

    /**
     * Create a check with <code>details</code> of a specific repository and commit described in <code>details</code>.
     *
     * @param context
     *         context of the check
     * @param details
     *         details of the check
     */
    public static void createCheckRun(ChecksContext context, ChecksDetails details) {
        String repository = context.getRepository();
        String headSha = context.getHeadSha();
        String token = context.getToken();

        if (StringUtils.isNoneBlank(repository, headSha, token)) {
            try {
                new GitHubBuilder().withAppInstallationToken(token).build()
                        .getRepository(repository).createCheckRun(details.getName(), headSha)
                        .withStartedAt(new Date())
                        .withDetailsURL(details.getDetailsURL())
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
    public static void updateCheckRun(ChecksContext context, ChecksDetails details) {
        String repository = context.getRepository();
        String headSha = context.getHeadSha();
        String token = context.getToken();

        if (StringUtils.isNoneBlank(repository, headSha, token)) {
            try {
                new GitHubBuilder().withAppInstallationToken(repository).build()
                        .getRepository(repository).createCheckRun(details.getName(), headSha)
                        .withStartedAt(new Date())
                        .withDetailsURL(details.getDetailsURL())
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
    public static void completeCheckRun(ChecksContext context, ChecksDetails details) {
        String repository = context.getRepository();
        String headSha = context.getHeadSha();
        String token = context.getToken();

        if (StringUtils.isNoneBlank(repository, headSha, token)) {
            try {
                new GitHubBuilder().withAppInstallationToken(token).build()
                        .getRepository(repository).createCheckRun(details.getName(), headSha)
                        .withCompletedAt(new Date())
                        .withDetailsURL(details.getDetailsURL())
                        .withStatus(Status.COMPLETED)
                        .withConclusion(Conclusion.SUCCESS)
                        .create();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not complete check run. Message: ", e);
            }
        }
    }
}
