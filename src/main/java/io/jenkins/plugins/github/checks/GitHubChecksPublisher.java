package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GitHub;

import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import io.jenkins.plugins.github.checks.api.ChecksDetails;

public class GitHubChecksPublisher extends ChecksPublisher {
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
        GHCheckRunBuilder builder = createBuilder(gitHub, details, context);
        builder.create();
    }

    @VisibleForTesting
    GHCheckRunBuilder createBuilder(final GitHub gitHub, final ChecksDetails details, final ChecksContext context)
            throws IOException {
        GHCheckRunBuilder builder = gitHub.getRepository(context.getRepository())
                .createCheckRun(details.getName(), Objects.requireNonNull(context.getHeadSha()));
        builder.withStatus(details.getStatus().toCheckRunStatus())
                .withDetailsURL(context.getURL());

        // TODO: Add output and Actions after completing the classes

        if (details.getConclusion() != ChecksConclusion.NONE) {
            builder.withConclusion(details.getConclusion().toCheckRunConclusion());
            builder.withCompletedAt(new Date());
        } else {
            builder.withStartedAt(new Date());
        }

        return builder;
    }
}
