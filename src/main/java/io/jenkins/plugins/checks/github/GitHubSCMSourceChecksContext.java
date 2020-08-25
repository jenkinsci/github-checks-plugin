package io.jenkins.plugins.checks.github;

import java.util.Optional;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;

import io.jenkins.plugins.util.PluginLogger;

/**
 * Provides a {@link GitHubChecksContext} for a Jenkins job that uses a supported {@link GitHubSCMSource}.
 */
class GitHubSCMSourceChecksContext extends GitHubChecksContext {
    /**
     * Creates a {@link GitHubSCMSourceChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param run a run of a GitHub Branch Source project
     */
    GitHubSCMSourceChecksContext(final Run<?, ?> run) {
        this(run, DisplayURLProvider.get().getRunURL(run), new SCMFacade());
    }

    /**
     * Creates a {@link GitHubSCMSourceChecksContext} according to the job. All attributes are computed during this period.
     *
     * @param job a GitHub Branch Source project
     */
    GitHubSCMSourceChecksContext(final Job<?, ?> job) {
        this(job, DisplayURLProvider.get().getJobURL(job), new SCMFacade());
    }

    @VisibleForTesting
    GitHubSCMSourceChecksContext(final Run<?, ?> run, final String runURL, final SCMFacade scmFacade) {
        super(run.getParent(), runURL, scmFacade);
    }

    @VisibleForTesting
    GitHubSCMSourceChecksContext(final Job<?, ?> job, final String jobURL, final SCMFacade scmFacade) {
        super(job, jobURL, scmFacade);
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

    @Override @CheckForNull
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

    @Override
    boolean isValid(final PluginLogger logger) {
        Job<?, ?> job = getJob();
        
        Optional<GitHubSCMSource> source = getScmFacade().findGitHubSCMSource(job);
        if (!source.isPresent()) {
            logger.log("No GitHub SCM source found");
            
            return false;
        }

        if (!hasValidCredentials(logger)) {
            return false;
        }

        logger.log("Using GitHub SCM source '%s' for GitHub checks", getRepository());
        
        return true;
    }
}
