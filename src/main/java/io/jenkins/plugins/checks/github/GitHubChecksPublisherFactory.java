package io.jenkins.plugins.checks.github;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;
import io.jenkins.plugins.util.PluginLogger;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * An factory which produces {@link GitHubChecksPublisher}.
 */
@Extension
public class GitHubChecksPublisherFactory extends ChecksPublisherFactory {
    private final SCMFacade scmFacade;
    private final DisplayURLProvider urlProvider;

    /**
     * Creates a new instance of {@link GitHubChecksPublisherFactory}.
     */
    public GitHubChecksPublisherFactory() {
        this(new SCMFacade(), DisplayURLProvider.get());
    }

    @VisibleForTesting
    GitHubChecksPublisherFactory(final SCMFacade scmFacade, final DisplayURLProvider urlProvider) {
        super();

        this.scmFacade = scmFacade;
        this.urlProvider = urlProvider;
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Run<?, ?> run, final TaskListener listener) {
        try {
            final String runURL = urlProvider.getRunURL(run);
            return createPublisher(listener, new GitSCMChecksContext(run, runURL, scmFacade),
                    new GitHubSCMSourceChecksContext(run, runURL, scmFacade));
        }
        catch (IOException e) {
            createConsoleLogger(listener).log("Could not create publisher.", e);
        }

        return Optional.empty();
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Job<?, ?> job, final TaskListener listener) {
        try {
            return createPublisher(listener, new GitHubSCMSourceChecksContext(job, urlProvider.getJobURL(job),
                    scmFacade));
        }
        catch (IOException e) {
            createConsoleLogger(listener).log("Could not create publisher.", e);
        }

        return Optional.empty();
    }

    private Optional<ChecksPublisher> createPublisher(final TaskListener listener, final GitHubChecksContext... contexts)
            throws IOException {
        try (ByteArrayOutputStream cause = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(cause, true, StandardCharsets.UTF_8.name())) {
            PluginLogger causeLogger = new PluginLogger(ps, "GitHub Checks");

            for (GitHubChecksContext ctx : contexts) {
                if (ctx.isValid(causeLogger)) {
                    return Optional.of(new GitHubChecksPublisher(ctx, createConsoleLogger(getListener(listener))));
                }
            }

            listener.getLogger().print(cause.toString(StandardCharsets.UTF_8.name()));
        }

        return Optional.empty();
    }

    private TaskListener getListener(final TaskListener taskListener) {
        // FIXME: checks-API should use a Null listener
        if (taskListener == null) {
            return TaskListener.NULL;
        }
        return taskListener;
    }

    private PluginLogger createConsoleLogger(final TaskListener listener) {
        return new PluginLogger(listener.getLogger(), "GitHub Checks");
    }
}
