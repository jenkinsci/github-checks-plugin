package io.jenkins.plugins.checks.github.status;

import edu.hm.hafner.util.VisibleForTesting;
import hudson.Extension;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import io.jenkins.plugins.checks.github.SCMFacade;
import io.jenkins.plugins.checks.status.StatusChecksProperties;
import jenkins.plugins.git.GitSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementing {@link StatusChecksProperties} to retrieve properties from jobs with
 * {@link GitHubSCMSource}, {@link GitSCM}, or {@link GitSCMSource}.
 */
@Extension
public class GitHubStatusChecksProperties implements StatusChecksProperties {
    private final SCMFacade scmFacade;

    /**
     * Default Constructor.
     */
    public GitHubStatusChecksProperties() {
        this(new SCMFacade());
    }

    @VisibleForTesting
    GitHubStatusChecksProperties(final SCMFacade facade) {
        this.scmFacade = facade;
    }

    @Override
    public boolean isApplicable(final Job<?, ?> job) {
        return getConfigurations(job).isPresent();
    }

    @Override
    public String getName(final Job<?, ?> job) {
        return getConfigurations(job).orElse(new DefaultGitHubStatusChecksConfigurations()).getName();
    }

    @Override
    public boolean isSkip(final Job<?, ?> job) {
        return getConfigurations(job).orElse(new DefaultGitHubStatusChecksConfigurations()).isSkip();
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
