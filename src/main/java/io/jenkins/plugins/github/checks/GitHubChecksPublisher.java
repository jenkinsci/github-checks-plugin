package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GitHubBuilder;

import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import hudson.Extension;

import io.jenkins.plugins.github.checks.api.ChecksDetails;
import io.jenkins.plugins.github.checks.api.ChecksPublisher;

@Extension
public class GitHubChecksPublisher extends ChecksPublisher {
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
            builder = new GitHubBuilder()
                    .withAppInstallationToken(context.getToken()).build()
                    .getRepository(context.getRepository())
                    .createCheckRun(details.getName(), Objects.requireNonNull(context.getHeadSha()))
                    .withDetailsURL(context.getRun().getParent().getAbsoluteUrl() + context.getRun().getNumber() + "/");
        } catch (IOException e) {
            throw new IOException("could not publish checks to GitHub", e);
        }

        // TODO: Add output and Actions, need a strategy mapping the output and actions to the library's

        switch (details.getStatus()) {
            case QUEUED:
                builder.withStatus(Status.QUEUED).withStartedAt(new Date()).create();
                break;
            case IN_PROGRESS:
                builder.withStatus(Status.IN_PROGRESS).withStartedAt(new Date()).create();
                break;
            case COMPLETED:
                builder.withCompletedAt(new Date())
                        .withConclusion(Conclusion.SUCCESS) // TODO: need a strategy mapping the conclusion
                        .create();
        }
    }

    @Override
    protected boolean isApplicable(final ChecksContext context) {
        return context.getSource() instanceof GitHubSCMSource;
    }
}
