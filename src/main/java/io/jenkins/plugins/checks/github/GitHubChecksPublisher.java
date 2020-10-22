package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.util.PluginLogger;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * A publisher which publishes GitHub check runs.
 */
public class GitHubChecksPublisher extends ChecksPublisher {
    private static final String GITHUB_URL = "https://api.github.com";
    private static final Logger SYSTEM_LOGGER = Logger.getLogger(GitHubChecksPublisher.class.getName());

    private final GitHubChecksContext context;
    private final PluginLogger buildLogger;
    private final String gitHubUrl;

    /**
     * {@inheritDoc}.
     *
     * @param context
     *         a context which contains SCM properties
     */
    public GitHubChecksPublisher(final GitHubChecksContext context, final PluginLogger buildLogger) {
        this(context, buildLogger, GITHUB_URL);
    }

    GitHubChecksPublisher(final GitHubChecksContext context, final PluginLogger buildLogger, final String gitHubUrl) {
        super();

        this.context = context;
        this.buildLogger = buildLogger;
        this.gitHubUrl = gitHubUrl;
    }

    /**
     * Publishes a GitHub check run.
     *
     * @param details
     *         the details of a check run
     */
    @Override
    public void publish(final ChecksDetails details) {
        try {
            GitHubAppCredentials credentials = context.getCredentials();
            GitHub gitHub = Connector.connect(StringUtils.defaultIfBlank(credentials.getApiUri(), gitHubUrl),
                    credentials);

            GitHubChecksDetails gitHubDetails = new GitHubChecksDetails(details);
            createBuilder(gitHub, gitHubDetails).create();
            buildLogger.log("GitHub check (name: %s, status: %s) has been published.", gitHubDetails.getName(),
                    gitHubDetails.getStatus());
            SYSTEM_LOGGER.fine(format("Published check for repo: %s, sha: %s, job name: %s, name: %s, status: %s",
                            context.getRepository(),
                            context.getHeadSha(),
                            context.getJob().getFullName(),
                            gitHubDetails.getName(),
                            gitHubDetails.getStatus()).replaceAll("[\r\n]", ""));
        }
        catch (IOException e) {
            String message = "Failed Publishing GitHub checks: ";
            SYSTEM_LOGGER.log(Level.WARNING, (message + details).replaceAll("[\r\n]", ""), e);
            buildLogger.log(message + e);
        }
    }

    @VisibleForTesting
    GHCheckRunBuilder createBuilder(final GitHub gitHub, final GitHubChecksDetails details) throws IOException {
        GHCheckRunBuilder builder = gitHub.getRepository(context.getRepository())
                .createCheckRun(details.getName(), context.getHeadSha())
                .withStatus(details.getStatus())
                .withExternalID(context.getJob().getFullName())
                .withDetailsURL(details.getDetailsURL().orElse(context.getURL()))
                .withStartedAt(details.getStartedAt().orElse(Date.from(Instant.now())));

        if (details.getConclusion().isPresent()) {
            builder.withConclusion(details.getConclusion().get())
                    .withCompletedAt(details.getCompletedAt().orElse(Date.from(Instant.now())));
        }

        details.getOutput().ifPresent(builder::add);
        details.getActions().forEach(builder::add);

        return builder;
    }
}
