package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GitHub;

import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import io.jenkins.plugins.checks.ChecksContext;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksPublisher;

class GitHubChecksPublisher extends ChecksPublisher {
    static final String GITHUB_URL = "https://api.github.com";

    public GitHubChecksPublisher(final ChecksContext context) {
        super(context);
    }

    /**
     * Publishes a GitHub check run.
     *
     * @param details
     *         the details of a check run
     * @throws IOException if publish the check run failed
     */
    @Override
    public void publish(final ChecksDetails details) throws IOException {
        GitHubAppCredentials credentials = context.getCredential();
        GitHub gitHub = Connector.connect(StringUtils.defaultIfBlank(credentials.getApiUri(), GITHUB_URL), credentials);
        GHCheckRunBuilder builder = createBuilder(gitHub, new GitHubChecksDetails(details), context);
        builder.create();
    }

    @VisibleForTesting
    GHCheckRunBuilder createBuilder(final GitHub gitHub, final GitHubChecksDetails details, final ChecksContext context)
            throws IOException {
        GHCheckRunBuilder builder = gitHub.getRepository(context.getRepository())
                .createCheckRun(details.getName(), Objects.requireNonNull(context.getHeadSha()));
        builder.withStatus(details.getStatus())
                .withDetailsURL(StringUtils.defaultIfBlank(details.getDetailsURL(), context.getURL()));

        // TODO: Add Actions after completing the classes

        if (details.getConclusion() != null) {
            builder.withConclusion(details.getConclusion());
            builder.withCompletedAt(new Date());
        } else {
            builder.withStartedAt(new Date());
        }

        return builder;
    }
}
