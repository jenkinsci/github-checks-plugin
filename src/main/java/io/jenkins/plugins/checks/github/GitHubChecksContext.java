package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;

import java.util.Optional;

/**
 * Provides check properties that should be resolved  Jenkins job.
 */
class GitHubChecksContext {
    @Nullable
    private final Run<?, ?> run;
    private final Job<?, ?> job;
    private final GitHubSCMFacade scmFacade;
    private final DisplayURLProvider urlProvider;

    /**
     * Creates a {@link GitHubChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param run a run of a GitHub Branch Source project
     */
    GitHubChecksContext(final Run<?, ?> run) {
        this(run, new GitHubSCMFacade(), DisplayURLProvider.get());
    }

    /**
     * Creates a {@link GitHubChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param job a run of a GitHub Branch Source project
     */
    GitHubChecksContext(final Job<?, ?> job) {
        this(job, new GitHubSCMFacade(), DisplayURLProvider.get());
    }

    @VisibleForTesting
    GitHubChecksContext(final Run<?, ?> run, final GitHubSCMFacade scmFacade, final DisplayURLProvider urlProvider) {
        this.job = run.getParent();
        this.run = run;
        this.scmFacade = scmFacade;
        this.urlProvider = urlProvider;
    }

    @VisibleForTesting
    @SuppressWarnings("PMD.NullAssignment")
        // run can be null when job is provided
    GitHubChecksContext(final Job<?, ?> job, final GitHubSCMFacade scmFacade, final DisplayURLProvider urlProvider) {
        this.job = job;
        this.run = null;
        this.scmFacade = scmFacade;
        this.urlProvider = urlProvider;
    }

    /**
     * Returns the Jenkins job.
     *
     * @return job for which the checks will be based on
     */
    public Job<?, ?> getJob() {
        return job;
    }

    /**
     * Returns the commit sha of the run.
     *
     * @return the commit sha of the run or null
     */
    public String getHeadSha() {
        String commit = resolveCommit();
        if (StringUtils.isEmpty(commit)) {
            return resolveHeadSha();
        }
        else {
            return commit;
        }
    }

    /**
     * Returns the source repository's full name of the run. The full name consists of the owner's name and the
     * repository's name, e.g. jenkins-ci/jenkins
     *
     * @return the source repository's full name
     */
    public String getRepository() {
        GitHubSCMSource source = resolveSource();
        return source.getRepoOwner() + "/" + source.getRepository();
    }

    /**
     * Returns the credentials to access the remote GitHub repository.
     *
     * @return the credentials or null
     */
    public GitHubAppCredentials getCredentials() {
        return resolveCredentials();
    }

    /**
     * Returns the URL of the run's summary page, e.g. https://ci.jenkins.io/job/Core/job/jenkins/job/master/2000/.
     *
     * @return the URL of the summary page
     */
    public String getURL() {
        if (run == null) {
            return urlProvider.getJobURL(job);
        }
        else {
            return urlProvider.getRunURL(run);
        }
    }

    private GitHubSCMSource resolveSource() {
        Optional<GitHubSCMSource> source
                = scmFacade.findGitHubSCMSource(job);
        if (!source.isPresent()) {
            throw new IllegalStateException("No GitHub SCM source available for job: " + job.getName());
        }

        return source.get();
    }

    private GitHubAppCredentials resolveCredentials() {
        String credentialsId = resolveSource().getCredentialsId();
        if (credentialsId == null) {
            throw new IllegalStateException("No credentials available for job: " + job.getName());
        }

        Optional<GitHubAppCredentials> foundCredentials
                = scmFacade.findGitHubAppCredentials(job, credentialsId);
        if (!foundCredentials.isPresent()) {
            throw new IllegalStateException("No GitHub APP credentials available for job: " + job.getName());
        }

        return foundCredentials.get();
    }

    private String resolveHeadSha() {
        Optional<SCMHead> head = scmFacade.findHead(job);
        if (!head.isPresent()) {
            throw new IllegalStateException("No SCM head available for job: " + job.getName());
        }

        Optional<SCMRevision> revision = scmFacade.findRevision(resolveSource(), head.get());
        if (!revision.isPresent()) {
            throw new IllegalStateException(
                    String.format("No SCM revision available for repository: %s and head: %s",
                            getRepository(), head.get().getName()));
        }

        return resolveCommit(revision.get());
    }

    private String resolveCommit() {
        if (run == null) {
            return StringUtils.EMPTY;
        }

        Optional<SCMRevision> revision = scmFacade.findRevision(resolveSource(), run);
        if (revision.isPresent()) {
            return resolveCommit(revision.get());
        }
        else {
            return StringUtils.EMPTY;
        }
    }

    private String resolveCommit(final SCMRevision revision) {
        if (revision instanceof AbstractGitSCMSource.SCMRevisionImpl) {
            return ((AbstractGitSCMSource.SCMRevisionImpl) revision).getHash();
        }
        else if (revision instanceof PullRequestSCMRevision) {
            return ((PullRequestSCMRevision) revision).getPullHash();
        }
        else {
            throw new IllegalStateException("Unsupported revision type: " + revision.getClass().getName());
        }
    }
}
