package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.VisibleForTesting;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;
import io.jenkins.plugins.checks.github.config.DefaultGitHubChecksConfig;
import io.jenkins.plugins.checks.github.config.GitHubChecksConfig;
import io.jenkins.plugins.util.PluginLogger;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import java.util.Optional;
import java.util.stream.Stream;

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
        return createPublisher(listener, getChecksConfig(run.getParent()),
                GitHubSCMSourceChecksContext.fromRun(run, runURL, scmFacade),
                new GitSCMChecksContext(run, runURL, scmFacade));
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Job<?, ?> job, final TaskListener listener) {
        return createPublisher(listener, getChecksConfig(job),
                GitHubSCMSourceChecksContext.fromJob(job, urlProvider.getJobURL(job), scmFacade));
    }

    private Optional<ChecksPublisher> createPublisher(final TaskListener listener, GitHubChecksConfig config,
                                                      final GitHubChecksContext... contexts) {
        FilteredLog causeLogger = new FilteredLog("Causes for no suitable publisher found: ");
        PluginLogger consoleLogger = new PluginLogger(listener.getLogger(), "GitHub Checks");

        for (GitHubChecksContext ctx : contexts) {
            if (ctx.isValid(causeLogger)) {
                return Optional.of(new GitHubChecksPublisher(ctx, consoleLogger));
            }
        }

        if (config.isVerboseConsoleLog()) {
            consoleLogger.logEachLine(causeLogger.getErrorMessages());
        }

        return Optional.empty();
    }

    private GitHubChecksConfig getChecksConfig(final Job<?, ?> job) {
        Optional<GitHubSCMSource> gitHubSCMSource = scmFacade.findGitHubSCMSource(job);
        if (gitHubSCMSource.isPresent()) {
            return getChecksConfig(gitHubSCMSource.get().getTraits().stream())
                    .orElseGet(DefaultGitHubChecksConfig::new);
        }

        Optional<GitSCM> gitSCM = scmFacade.findGitSCM(job);
        return gitSCM.map(scm -> getChecksConfig(scm.getExtensions().stream())
                .orElse(new DefaultGitHubChecksConfig()))
                .orElseGet(DefaultGitHubChecksConfig::new);

    }

    private Optional<GitHubChecksConfig> getChecksConfig(final Stream<?> stream) {
        return stream.filter(t -> t instanceof GitHubChecksConfig)
                .findFirst()
                .map(t -> (GitHubChecksConfig) t);
    }
}
