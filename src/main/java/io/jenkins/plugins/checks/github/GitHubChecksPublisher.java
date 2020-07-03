package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GitHub;

import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import hudson.model.Run;

import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksPublisher;

/**
 * A publisher which publishes GitHub check runs.
 */
public class GitHubChecksPublisher extends ChecksPublisher {
    private final Run<?, ?> run;

    /**
     * {@inheritDoc}.
     *
     * @param run
     *         a run of a GitHub branch source project
     */
    public GitHubChecksPublisher(final Run<?, ?> run) {
        super();

        this.run = run;
    }

    private static final Logger LOGGER = Logger.getLogger(GitHubChecksPublisher.class.getName());
    private static final String GITHUB_URL = "https://api.github.com";

    /**
     * Publishes a GitHub check run.
     *
     * @param details
     *         the details of a check run
     */
    @Override
    public void publish(final ChecksDetails details) {
        try {
            GitHubChecksContext context = new GitHubChecksContext(run);
            GitHubAppCredentials credentials = context.getCredentials();
            GitHub gitHub = Connector.connect(StringUtils.defaultIfBlank(credentials.getApiUri(), GITHUB_URL),
                    credentials);
            GHCheckRunBuilder builder = createBuilder(gitHub, new GitHubChecksDetails(details), context);
            builder.create();
        }
        catch (IllegalStateException | IOException e) {
            //TODO: log to the build console
            LOGGER.log(Level.WARN, "Could not publish GitHub check run", e);
        }
    }

    @VisibleForTesting
    GHCheckRunBuilder createBuilder(final GitHub gitHub, final GitHubChecksDetails details,
            final GitHubChecksContext context) throws IOException {
        GHCheckRunBuilder builder = gitHub.getRepository(context.getRepository())
                .createCheckRun(details.getName(), context.getHeadSha())
                .withStatus(details.getStatus())
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
