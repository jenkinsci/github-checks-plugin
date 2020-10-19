package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.VisibleForTesting;
import hudson.Extension;
import hudson.model.Job;
import io.jenkins.plugins.checks.status.StatusChecksProperties;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import java.util.Optional;

/**
 * Implementing {@link StatusChecksProperties} to retrieve properties from jobs with
 * {@link GitHubSCMSourceStatusChecksTrait}.
 */
@Extension
public class GitHubSCMSourceStatusChecksProperties implements StatusChecksProperties {
    private final SCMFacade scmFacade;

    /**
     * Default constructor.
     */
    public GitHubSCMSourceStatusChecksProperties() {
        this(new SCMFacade());
    }

    @VisibleForTesting
    GitHubSCMSourceStatusChecksProperties(final SCMFacade scmFacade) {
        this.scmFacade = scmFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final Job<?, ?> job) {
        return scmFacade.findGitHubSCMSource(job).isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(final Job<?, ?> job) {
        Optional<GitHubSCMSource> source = scmFacade.findGitHubSCMSource(job);
        if (!source.isPresent()) {
            return "";
        }

        return getStatusChecksTrait(source.get()).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSkipped(final Job<?, ?> job) {
        Optional<GitHubSCMSource> source = scmFacade.findGitHubSCMSource(job);
        return source.filter(s -> getStatusChecksTrait(s).isSkip()).isPresent();
    }

    private GitHubSCMSourceStatusChecksTrait getStatusChecksTrait(final GitHubSCMSource source) {
        return source.getTraits()
                .stream()
                .filter(t -> t instanceof GitHubSCMSourceStatusChecksTrait)
                .findFirst()
                .map(t -> (GitHubSCMSourceStatusChecksTrait)t)
                .orElseThrow(IllegalStateException::new);
    }
}
