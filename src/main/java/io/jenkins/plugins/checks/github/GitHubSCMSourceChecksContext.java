package io.jenkins.plugins.checks.github;

import java.util.Optional;

import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;

import edu.hm.hafner.util.VisibleForTesting;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;

import hudson.model.Job;
import hudson.model.Run;

/**
 * Provides check properties that should be resolved  Jenkins job.
 */
class GitHubSCMSourceChecksContext extends GitHubChecksContext {
    /**
     * Creates a {@link GitHubSCMSourceChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param run a run of a GitHub Branch Source project
     */
    GitHubSCMSourceChecksContext(final Run<?, ?> run) {
        this(run, new GitHubSCMFacade(), DisplayURLProvider.get());
    }

    /**
     * Creates a {@link GitHubSCMSourceChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param job a run of a GitHub Branch Source project
     */
    GitHubSCMSourceChecksContext(final Job<?, ?> job) {
        this(job, new GitHubSCMFacade(), DisplayURLProvider.get());
    }

    @VisibleForTesting
    GitHubSCMSourceChecksContext(final Run<?, ?> run, final GitHubSCMFacade scmFacade, final DisplayURLProvider urlProvider) {
        super(run.getParent(), run, scmFacade, urlProvider);
    }

    @VisibleForTesting
    @SuppressWarnings("PMD.NullAssignment")
        // run can be null when job is provided
    GitHubSCMSourceChecksContext(final Job<?, ?> job, final GitHubSCMFacade scmFacade, final DisplayURLProvider urlProvider) {
        super(job, null, scmFacade, urlProvider);
    }

    @Override
    public String getHeadSha() {
        return resolveHeadSha();
    }

    @Override
    public String getRepository() {
        GitHubSCMSource source = resolveSource();
        return source.getRepoOwner() + "/" + source.getRepository();
    }

    @Override
    protected String getCredentialsId() {
        return resolveSource().getCredentialsId();
    }

    private GitHubSCMSource resolveSource() {
        Optional<GitHubSCMSource> source = getScmFacade().findGitHubSCMSource(getJob());
        if (!source.isPresent()) {
            throw new IllegalStateException("No GitHub SCM source available for job: " + getJob().getName());
        }

        return source.get();
    }

    private String resolveHeadSha() {
        Optional<SCMHead> head = getScmFacade().findHead(getJob());
        if (!head.isPresent()) {
            throw new IllegalStateException("No SCM head available for job: " + getJob().getName());
        }

        Optional<SCMRevision> revision = getScmFacade().findRevision(resolveSource(), head.get());
        if (!revision.isPresent()) {
            throw new IllegalStateException(
                    String.format("No SCM revision available for repository: %s and head: %s",
                            getRepository(), head.get().getName()));
        }

        if (revision.get() instanceof AbstractGitSCMSource.SCMRevisionImpl) {
            return ((AbstractGitSCMSource.SCMRevisionImpl) revision.get()).getHash();
        }
        else if (revision.get() instanceof PullRequestSCMRevision) {
            return ((PullRequestSCMRevision) revision.get()).getPullHash();
        }
        else {
            throw new IllegalStateException("Unsupported revision type: " + revision.get().getClass().getName());
        }
    }
}
