package io.jenkins.plugins.checks.github;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Job;
import hudson.model.Run;
import hudson.security.ACL;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMRevisionAction;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Facade to GitHub SCM in Jenkins, used for finding GitHub SCM of a job.
 */
public class GitHubSCMFacade {
    /**
     * Find {@link GitHubSCMSource} (or GitHub repository) used by the {@code job}.
     *
     * @param job
     *         the Jenkins project
     * @return the found GitHub SCM source used or empty
     */
    public Optional<GitHubSCMSource> findGitHubSCMSource(final Job<?, ?> job) {
        SCMSource source = SCMSource.SourceByItem.findSource(job);
        return source instanceof GitHubSCMSource ? Optional.of((GitHubSCMSource) source) : Optional.empty();
    }

    /**
     * Find {@link GitHubAppCredentials} with the {@code credentialsId} used by the {@code job}.
     *
     * @param job
     *         the Jenkins project
     * @param credentialsId
     *         the id of the target credentials
     * @return the found GitHub App credentials or empty
     */
    public Optional<GitHubAppCredentials> findGitHubAppCredentials(final Job<?, ?> job, final String credentialsId) {
        List<GitHubAppCredentials> credentials = CredentialsProvider.lookupCredentials(
                GitHubAppCredentials.class, job, ACL.SYSTEM, Collections.emptyList());
        GitHubAppCredentials appCredentials =
                CredentialsMatchers.firstOrNull(credentials, CredentialsMatchers.withId(credentialsId));
        return Optional.ofNullable(appCredentials);
    }

    /**
     * Find {@link SCMHead} (or branch) used by the {@code job}.
     *
     * @param job
     *         the Jenkins project
     * @return the found SCM head or empty
     */
    public Optional<SCMHead> findHead(final Job<?, ?> job) {
        SCMHead head = SCMHead.HeadByItem.findHead(job);
        return Optional.ofNullable(head);
    }

    /**
     * Fetch the current {@link SCMRevision} used by the {@code head} of the {@code source}.
     *
     * @param source
     *         the GitHub repository
     * @param head
     *         the branch
     * @return the fetched revision or empty
     */
    public Optional<SCMRevision> findRevision(final GitHubSCMSource source, final SCMHead head) {
        try {
            return Optional.ofNullable(source.fetch(head, null));
        }
        catch (IOException | InterruptedException e) {
            throw new IllegalStateException(String.format("Could not fetch revision from repository: %s and branch: %s",
                    source.getRepoOwner() + "/" + source.getRepository(), head.getName()), e);
        }
    }

    /**
     * Find the commit from given {@code build}.
     *
     * @param source
     *         the scm source the {@code build} is using
     * @param build
     *         the target build
     * @return the found commit
     */
    public String findHeadCommit(final GitHubSCMSource source, final Run<?, ?> build) {
        SCMRevision revision = SCMRevisionAction.getRevision(source, build);
        if (revision instanceof AbstractGitSCMSource.SCMRevisionImpl) {
            return ((AbstractGitSCMSource.SCMRevisionImpl)revision).getHash();
        } else if (revision instanceof PullRequestSCMRevision) {
            return ((PullRequestSCMRevision) revision).getPullHash();
        } else {
            throw new IllegalArgumentException("Unsupported revision " + revision);
        }
    }
}
