package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.github.GHCheckRun;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GitHub;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.util.PluginLogger;

import static java.lang.String.*;

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
     * Creates a new instance of GitHubChecksPublisher.
     *
     * @param context
     *         a context which contains SCM properties
     * @param buildLogger
     *         the logger to use
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
     * @param details the details of a check run
     */
    @Override
    public void publish(final ChecksDetails details) {
        try {
            final var credentials = context.getCredentials();

            // Prevent publication with unsupported credential types
            switch (credentials.getClass().getSimpleName()) {
                case "GitHubAppCredentials":
                case "VaultUsernamePasswordCredentialImpl":
                    break;
                default:
                    return;
            }

            String apiUri = null;
            if (credentials instanceof GitHubAppCredentials) {
                apiUri = ((GitHubAppCredentials) credentials).getApiUri();
            }

            GitHub gitHub = Connector.connect(StringUtils.defaultIfBlank(apiUri, gitHubUrl),
                credentials);

            GitHubChecksDetails gitHubDetails = new GitHubChecksDetails(details);

            Optional<Long> existingId = context.getId(gitHubDetails.getName());

            final GHCheckRun run;

            if (existingId.isPresent()) {
                run = getUpdater(gitHub, gitHubDetails, existingId.get()).create();
            }
            else {
                run = getCreator(gitHub, gitHubDetails).create();
            }

            context.addActionIfMissing(run.getId(), gitHubDetails.getName());

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
            buildLogger.log("%s", message + e);
        }
    }

    @VisibleForTesting
    GHCheckRunBuilder getUpdater(final GitHub github, final GitHubChecksDetails details, final long checkId)
            throws IOException {
        GHCheckRunBuilder builder = github.getRepository(context.getRepository())
                .updateCheckRun(checkId);

        return applyDetails(builder, details);
    }

    @VisibleForTesting
    GHCheckRunBuilder getCreator(final GitHub gitHub, final GitHubChecksDetails details) throws IOException {
        GHCheckRunBuilder builder = gitHub.getRepository(context.getRepository())
            .createCheckRun(details.getName(), context.getHeadSha())
            .withStartedAt(details.getStartedAt().orElse(Date.from(Instant.now())));

        return applyDetails(builder, details);
    }

    @VisibleForTesting
    GitHubChecksContext getContext() {
        return context;
    }

    private GHCheckRunBuilder applyDetails(final GHCheckRunBuilder builder, final GitHubChecksDetails details) {
        builder
                .withStatus(details.getStatus())
                .withDetailsURL(details.getDetailsURL().orElse(context.getURL()));

        if (context.getRun().isPresent()) {
            final String externalId = context.getRun().get().getExternalizableId();
            builder.withExternalID(externalId);
        }

        if (details.getConclusion().isPresent()) {
            builder.withConclusion(details.getConclusion().get())
                    .withCompletedAt(details.getCompletedAt().orElse(Date.from(Instant.now())));
        }

        details.getOutput().ifPresent(builder::add);
        details.getActions().forEach(builder::add);

        return builder;
    }

}
