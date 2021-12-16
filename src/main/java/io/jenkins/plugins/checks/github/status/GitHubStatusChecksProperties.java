package io.jenkins.plugins.checks.github.status;

import java.util.Optional;
import java.util.stream.Stream;

import edu.hm.hafner.util.VisibleForTesting;

import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import hudson.Extension;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSCMSource;

import io.jenkins.plugins.checks.github.SCMFacade;
import io.jenkins.plugins.checks.status.AbstractStatusChecksProperties;

/**
 * Implementing {@link io.jenkins.plugins.checks.status.AbstractStatusChecksProperties} to retrieve properties
 * from jobs with {@link GitHubSCMSource}, {@link GitSCM}, or {@link GitSCMSource}.
 */
@Extension
public class GitHubStatusChecksProperties extends AbstractStatusChecksProperties {
    private static final GitHubStatusChecksConfigurations DEFAULT_CONFIGURATION
            = new DefaultGitHubStatusChecksConfigurations();

    private final SCMFacade scmFacade;

    /**
     * Default Constructor.
     */
    public GitHubStatusChecksProperties() {
        this(new SCMFacade());
    }

    @VisibleForTesting
    GitHubStatusChecksProperties(final SCMFacade facade) {
        super();

        this.scmFacade = facade;
    }

    @Override
    public boolean isApplicable(final Job<?, ?> job) {
        return scmFacade.findGitHubSCMSource(job).isPresent() || scmFacade.findGitSCM(job).isPresent();
    }

    @Override
    public String getName(final Job<?, ?> job) {
        return getConfigurations(job).orElse(DEFAULT_CONFIGURATION).getName();
    }

    @Override
    public boolean isSkipped(final Job<?, ?> job) {
        return getConfigurations(job).orElse(DEFAULT_CONFIGURATION).isSkip();
    }

    @Override
    public boolean isUnstableBuildNeutral(final Job<?, ?> job) {
        return getConfigurations(job).orElse(DEFAULT_CONFIGURATION).isUnstableBuildNeutral();
    }

    @Override
    public boolean isSuppressLogs(final Job<?, ?> job) {
        return getConfigurations(job).orElse(DEFAULT_CONFIGURATION).isSuppressLogs();
    }

    @Override
    public boolean isSkipProgressUpdates(final Job<?, ?> job) {
        return getConfigurations(job).orElse(DEFAULT_CONFIGURATION).isSkipProgressUpdates();
    }

    private Optional<GitHubStatusChecksConfigurations> getConfigurations(final Job<?, ?> job) {
        Optional<GitHubSCMSource> gitHubSCMSource = scmFacade.findGitHubSCMSource(job);
        if (gitHubSCMSource.isPresent()) {
            return getConfigurations(gitHubSCMSource.get().getTraits().stream());
        }

        Optional<GitSCM> gitSCM = scmFacade.findGitSCM(job);
        if (gitSCM.isPresent()) {
            return getConfigurations(gitSCM.get().getExtensions().stream());
        }

        return Optional.empty();
    }

    private Optional<GitHubStatusChecksConfigurations> getConfigurations(final Stream<?> stream) {
        return stream.filter(t -> t instanceof GitHubStatusChecksConfigurations)
                .findFirst()
                .map(t -> (GitHubStatusChecksConfigurations) t);
    }
}
