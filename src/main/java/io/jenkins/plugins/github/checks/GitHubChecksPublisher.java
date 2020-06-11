package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GitHubBuilder;

import io.jenkins.plugins.github.checks.api.ChecksDetails;

public class GitHubChecksPublisher extends ChecksPublisher {
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
        GHCheckRunBuilder builder;
        try {
            builder = new GitHubBuilder() // TODO: Adapt to branch-source connector
                    .withAppInstallationToken(context.getToken()).build()
                    .getRepository(context.getRepository())
                    .createCheckRun(details.getName(), Objects.requireNonNull(context.getHeadSha()))
                    // TODO: encapsulate the logic getting URL
                    .withDetailsURL(context.getRun().getParent().getAbsoluteUrl() + context.getRun().getNumber() + "/");
        } catch (IOException e) {
            throw new IOException("could not publish checks to GitHub", e);
        }

        // TODO: Add output and Actions, need a strategy mapping the output and actions to the library's

        switch (details.getStatus()) {
            case QUEUED: // TODO: Add prefix for enum
                builder.withStatus(Status.QUEUED).withStartedAt(new Date()).create();
                break;
            case IN_PROGRESS:
                builder.withStatus(Status.IN_PROGRESS).withStartedAt(new Date()).create();
                break;
            case COMPLETED:
                builder.withCompletedAt(new Date())
                        .withConclusion(Conclusion.SUCCESS) // TODO: add a resolver?
                        .create();
        }
    }
}
