package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.VisibleForTesting;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;
import io.jenkins.plugins.util.PluginLogger;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

import java.util.Optional;

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
        final String runURL = urlProvider.getRunURL(run);
        return createPublisher(listener, GitHubSCMSourceChecksContext.fromRun(run, runURL, scmFacade),
                new GitSCMChecksContext(run, runURL, scmFacade));
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Job<?, ?> job, final TaskListener listener) {
        return createPublisher(listener, GitHubSCMSourceChecksContext.fromJob(job, urlProvider.getJobURL(job), scmFacade));
    }

    private Optional<ChecksPublisher> createPublisher(final TaskListener listener, final GitHubChecksContext... contexts) {
        FilteredLog causeLogger = new FilteredLog("Causes for no suitable publisher found: ");
        PluginLogger consoleLogger = new PluginLogger(listener.getLogger(), "GitHub Checks");

        for (GitHubChecksContext ctx : contexts) {
            if (ctx.isValid(causeLogger)) {
                return Optional.of(new GitHubChecksPublisher(ctx, consoleLogger));
            }
        }

        consoleLogger.logEachLine(causeLogger.getErrorMessages());
        return Optional.empty();
    }
}
