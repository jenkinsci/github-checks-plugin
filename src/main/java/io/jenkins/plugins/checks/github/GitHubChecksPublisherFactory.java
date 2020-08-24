package io.jenkins.plugins.checks.github;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger LOGGER = Logger.getLogger(ChecksPublisherFactory.class.getName());

    private final SCMFacade scmFacade;

    /**
     * Creates a new instance of {@link GitHubChecksPublisherFactory}.
     */
    public GitHubChecksPublisherFactory() {
        this(new SCMFacade());
    }

    @VisibleForTesting
    GitHubChecksPublisherFactory(final SCMFacade scmFacade) {
        super();

        this.scmFacade = scmFacade;
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Run<?, ?> run, final TaskListener listener) {
        try {
            return createPublisher(run, DisplayURLProvider.get().getRunURL(run), listener);
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not create logger.", e);
        }

        return Optional.empty();
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Job<?, ?> job, final TaskListener listener) {
        try {
            return createPublisher(job, DisplayURLProvider.get().getJobURL(job), listener);
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not create logger.", e);
        }

        return Optional.empty();
    }

    @VisibleForTesting
    Optional<ChecksPublisher> createPublisher(final Run<?, ?> run, final String runURL, final TaskListener listener)
            throws IOException {
        try (ByteArrayOutputStream cause = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(cause, true, StandardCharsets.UTF_8.name())) {
            PluginLogger causeLogger = new PluginLogger(ps, "GitHub Checks");

            GitSCMChecksContext gitSCMContext = new GitSCMChecksContext(run, runURL, scmFacade);
            if (gitSCMContext.isValid(causeLogger)) {
                return Optional.of(new GitHubChecksPublisher(gitSCMContext, createConsoleLogger(getListener(listener))));
            }

            GitHubSCMSourceChecksContext gitHubSCMSourceContext = new GitHubSCMSourceChecksContext(run, runURL, scmFacade);
            if (gitHubSCMSourceContext.isValid(causeLogger)) {
                return Optional.of(new GitHubChecksPublisher(gitHubSCMSourceContext, createConsoleLogger(getListener(listener))));
            }

            listener.getLogger().print(cause.toString(StandardCharsets.UTF_8.name()));
        }

        return Optional.empty();
    }

    @VisibleForTesting
    Optional<ChecksPublisher> createPublisher(final Job<?, ?> job, final String jobURL, final TaskListener listener)
            throws IOException {
        try (ByteArrayOutputStream cause = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(cause, true, StandardCharsets.UTF_8.name())) {
            PluginLogger causeLogger = new PluginLogger(ps, "GitHub Checks");

            GitHubSCMSourceChecksContext gitHubSCMSourceContext = new GitHubSCMSourceChecksContext(job, jobURL, scmFacade);
            if (gitHubSCMSourceContext.isValid(causeLogger)) {
                return Optional.of(new GitHubChecksPublisher(gitHubSCMSourceContext, createConsoleLogger(getListener(listener))));
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
