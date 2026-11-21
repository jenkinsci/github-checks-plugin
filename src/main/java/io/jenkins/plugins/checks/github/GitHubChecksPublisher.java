package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.github.GHCheckRun;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GHFileNotFoundException;
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
    private static final Pattern SIZE_EXCEEDED_PATTERN =
            Pattern.compile("Only (\\d+) characters are allowed; (\\d+) were supplied", Pattern.CASE_INSENSITIVE);
    /**
     * Waits before each retry of an update that got 404 Not Found. In a probe, all such answers came in the first
     * 3 seconds after the check run was created.
     */
    @VisibleForTesting
    static final long[] UPDATE_RETRY_DELAYS_MILLIS = {1_000, 2_000, 4_000};

    @VisibleForTesting
    static Sleeper sleeper = Thread::sleep;

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

            GHCheckRun run;

            try {
                if (existingId.isPresent()) {
                    run = update(gitHub, gitHubDetails, existingId.get());
                }
                else {
                    run = getCreator(gitHub, gitHubDetails).create();
                }
            }
            catch (IOException e) {
                Optional<Integer> reducedLimit = calculateReducedLimit(e);
                if (reducedLimit.isPresent()) {
                    buildLogger.log("GitHub check publication failed due to character limit, retrying with max message size %d: %s",
                            reducedLimit.get(), e.getMessage());
                    GitHubChecksDetails retryDetails = new GitHubChecksDetails(details, reducedLimit.get());
                    if (existingId.isPresent()) {
                        run = update(gitHub, retryDetails, existingId.get());
                    }
                    else {
                        run = getCreator(gitHub, retryDetails).create();
                    }
                }
                else {
                    throw e;
                }
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

    static Optional<Integer> calculateReducedLimit(final Throwable throwable) {
        String msg = throwable != null ? throwable.getMessage() : null;
        if (msg == null || msg.isEmpty() || msg.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = SIZE_EXCEEDED_PATTERN.matcher(msg);
        if (matcher.find()) {
            try {
                int allowed = Integer.parseInt(matcher.group(1));
                int supplied = Integer.parseInt(matcher.group(2));
                int overshoot = Math.max(0, supplied - allowed);
                // Chop off at least overshoot + safety margin (e.g. 100 chars)
                int reduced = Math.max(100, allowed - overshoot - 100);
                return Optional.of(reduced);
            }
            catch (NumberFormatException ignored) {
                // ignore
            }
        }
        return Optional.empty();
    }

    /**
     * Updates a check run, and retries when GitHub answers 404 Not Found.
     *
     * <p>
     * For a few seconds after GitHub creates a check run, an update of it can get 404 Not Found, although the check
     * run exists. A completed check that gets this answer stays in progress on GitHub for ever. An update that GitHub
     * does not accept after the last retry fails as before.
     * </p>
     */
    private GHCheckRun update(final GitHub gitHub, final GitHubChecksDetails details, final long checkId)
            throws IOException {
        for (int attempt = 0; ; attempt++) {
            try {
                return getUpdater(gitHub, details, checkId).create();
            }
            catch (GHFileNotFoundException e) {
                if (attempt >= UPDATE_RETRY_DELAYS_MILLIS.length) {
                    throw e;
                }
                long delay = UPDATE_RETRY_DELAYS_MILLIS[attempt];
                buildLogger.log("GitHub did not find check run %d (name: %s) to update it, retrying in %d ms",
                        checkId, details.getName(), delay);
                try {
                    sleeper.sleep(delay);
                }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
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

    /**
     * Waits for a given time. Tests replace it so that they do not wait.
     */
    @VisibleForTesting
    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }
}
