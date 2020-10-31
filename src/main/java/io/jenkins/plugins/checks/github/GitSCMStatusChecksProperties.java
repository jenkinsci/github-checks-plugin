package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.VisibleForTesting;
import hudson.Extension;
import hudson.model.Job;
import io.jenkins.plugins.checks.status.StatusChecksProperties;

import java.util.Optional;

/**
 * Implementing {@link StatusChecksProperties} to retrieve properties from jobs with
 * {@link hudson.plugins.git.GitSCM} or {@link jenkins.plugins.git.GitSCMSource}.
 */
@Extension
public class GitSCMStatusChecksProperties implements StatusChecksProperties {
    private final SCMFacade scmFacade;

    /**
     * Default constructor.
     */
    public GitSCMStatusChecksProperties() {
        this(new SCMFacade());
    }

    @VisibleForTesting
    GitSCMStatusChecksProperties(final SCMFacade scmFacade) {
        this.scmFacade = scmFacade;
    }

    /**
     * Only applicable to jobs using {@link hudson.plugins.git.GitSCM} or {@link jenkins.plugins.git.GitSCMSource}.
     *
     * @param job
     *         a jenkins job
     * @return true if {@code job} is using {@link hudson.plugins.git.GitSCM} or {@link jenkins.plugins.git.GitSCMSource}.
     */
    @Override
    public boolean isApplicable(final Job<?, ?> job) {
        return scmFacade.findGitSCM(job).isPresent();
    }

    /**
     * Returns the name configured by user in the {@link GitSCMStatusChecksExtension} or {@link GitSCMSourceStatusChecksTrait}.
     *
     * @param job
     *         a jenkins job
     * @return name of status checks if configured, or a default "Jenkins" will be used.
     */
    @Override
    public String getName(final Job<?, ?> job) {
        return getGitSCMStatusChecksExtension(job)
                .map(GitSCMStatusChecksExtension::getName)
                .orElse("Jenkins");
    }

    /**
     * Returns if skip publishing status checks as user configured in the {@link GitSCMStatusChecksExtension}
     * or {@link GitSCMSourceStatusChecksTrait}.
     *
     * @param job
     *         a jenkins job
     * @return true to skip publishing checks if configured.
     */
    @Override
    public boolean isSkip(final Job<?, ?> job) {
        return getGitSCMStatusChecksExtension(job)
                .map(GitSCMStatusChecksExtension::isSkip)
                .orElse(false);
    }

    private Optional<GitSCMStatusChecksExtension> getGitSCMStatusChecksExtension(final Job<?, ?> job) {
        return scmFacade.findGitSCM(job).map(scm -> scm.getExtensions().get(GitSCMStatusChecksExtension.class));
    }
}
