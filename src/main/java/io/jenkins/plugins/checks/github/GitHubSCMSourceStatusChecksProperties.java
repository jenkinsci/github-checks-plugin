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
     * Only applicable to jobs using {@link GitHubSCMSource}.
     *
     * @param job
     *         a jenkins job
     * @return true if {@code job} is using {@link GitHubSCMSource}
     */
    @Override
    public boolean isApplicable(final Job<?, ?> job) {
        return scmFacade.findGitHubSCMSource(job).isPresent();
    }

    /**
     * Returns the name configured by user in the {@link GitHubSCMSourceStatusChecksTrait}.
     *
     * @param job
     *         a jenkins job
     * @return name of status checks if configured, or a default "Jenkins" will be used.
     */
    @Override
    public String getName(final Job<?, ?> job) {
        return scmFacade.findGitHubSCMSource(job)
                .map(gitHubSCMSource -> getStatusChecksTrait(gitHubSCMSource)
                        .map(GitHubSCMSourceStatusChecksTrait::getName).orElse("Jenkins"))
                .orElse("Jenkins");
    }

    /**
     * Returns if skip publishing status checks as user configured in the {@link GitHubSCMSourceStatusChecksTrait}.
     *
     * @param job
     *         a jenkins job
     * @return true to skip publishing checks if configured.
     */
    @Override
    public boolean isSkip(final Job<?, ?> job) {
        return scmFacade.findGitHubSCMSource(job)
                .map(s -> getStatusChecksTrait(s)
                        .map(GitHubSCMSourceStatusChecksTrait::isSkip).orElse(true))
                .orElse(true);

    }

    private Optional<GitHubSCMSourceStatusChecksTrait> getStatusChecksTrait(final GitHubSCMSource source) {
        return source.getTraits()
                .stream()
                .filter(t -> t instanceof GitHubSCMSourceStatusChecksTrait)
                .findFirst()
                .map(t -> (GitHubSCMSourceStatusChecksTrait)t);
    }
}
