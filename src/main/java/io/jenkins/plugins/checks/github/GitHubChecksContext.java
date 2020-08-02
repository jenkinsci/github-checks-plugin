package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.VisibleForTesting;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.util.JenkinsFacade;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;

import java.util.Optional;

/**
 * Provides check properties that should be resolved from Jenkins job.
 */
class GitHubChecksContext {
    private GitHubSCMSource source;
    private GitHubAppCredentials credentials;
    private String headSha;
    private String url;
    private final GitHubSCMFacade scmFacade;
    private final Job<?, ?> job;

    /**
     * Creates a {@link GitHubChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param job
     *         a run of a GitHub Branch Source project
     */
    GitHubChecksContext(final Job<?, ?> job) {
        this(job, new GitHubSCMFacade());
    }

    @VisibleForTesting
    GitHubChecksContext(final Job<?, ?> job, final GitHubSCMFacade scmFacade) {
        this.job = job;
        this.scmFacade = scmFacade;
    }

    /**
     * Returns the commit sha of the run.
     *
     * @return the commit sha of the run or null
     */
    public String getHeadSha() {
        return resolveHeadSha();
    }

    /**
     * Returns the source repository's full name of the run. The full name consists of the owner's name and the
     * repository's name, e.g. jenkins-ci/jenkins
     *
     * @return the source repository's full name
     */
    public String getRepository() {
        resolveSource();
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
        return resolveURL(new JenkinsFacade());
    }

    private GitHubSCMSource resolveSource() {
        if (source == null) {
            Optional<GitHubSCMSource> foundSource
                    = scmFacade.findGitHubSCMSource(job);
            if (!foundSource.isPresent()) {
                throw new IllegalStateException("No GitHub SCM source available for job: " + job.getName());
            }

            source = foundSource.get();
        }

        return source;
    }

    private GitHubAppCredentials resolveCredentials() {
        if (credentials == null) {
            Optional<GitHubAppCredentials> foundCredentials
                    = scmFacade.findGitHubAppCredentials(job, resolveSource().getCredentialsId());
            if (!foundCredentials.isPresent()) {
                throw new IllegalStateException("No GitHub APP credentials available for job: " + job.getName());
            }

            credentials = foundCredentials.get();
        }

        return credentials;
    }

    private String resolveHeadSha() {
        if (headSha == null) {
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

            if (revision.get() instanceof AbstractGitSCMSource.SCMRevisionImpl) {
                headSha = ((AbstractGitSCMSource.SCMRevisionImpl) revision.get()).getHash();
            }
            else if (revision.get() instanceof PullRequestSCMRevision) {
                headSha = ((PullRequestSCMRevision) revision.get()).getPullHash();
            }
            else {
                throw new IllegalStateException("Unsupported revision type: " + revision.get().getClass().getName());
            }
        }

        return headSha;
    }

    private String resolveURL(final JenkinsFacade jenkinsFacade) {
        if (url == null) {
            Run<?, ?> lastBuild = job.getLastBuild();
            if (lastBuild.isLogUpdated()) {
                url = jenkinsFacade.getAbsoluteUrl(lastBuild.getUrl());
            }
            else {
                String[] tokens = lastBuild.getUrl().split("/");
                tokens[tokens.length - 1] = String.valueOf(job.getNextBuildNumber());
                url = jenkinsFacade.getAbsoluteUrl(tokens);
            }
        }

        return url;
    }

    @VisibleForTesting
    String getURL(final JenkinsFacade jenkinsFacade) {
        return resolveURL(jenkinsFacade);
    }
}
