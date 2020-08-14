package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;

import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
import hudson.security.ACL;

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
    public SCMSource findSCMSource(final Job<?, ?> job) {
        return SCMSource.SourceByItem.findSource(job);
    }

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
     * Finds the {@link GitSCM} used by the {@code run}.
     *
     * @param run
     *         the run to get the SCM from 
     * @return the found GitSCM or empty
     */
    public Optional<GitSCM> findGitSCM(final Run<?, ?> run) {
        SCM scm = new ScmResolver().getScm(run);

        if (scm instanceof GitSCM) {
            return Optional.of((GitSCM) scm);
        }

        return Optional.empty();
    }

    UserRemoteConfig getUserRemoteConfig(final GitSCM scm) {
        List<UserRemoteConfig> configs = scm.getUserRemoteConfigs();
        if (configs.isEmpty()) {
            return new UserRemoteConfig(null, null, null, null);
        }
        return configs.get(0);
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
    public Optional<SCMRevision> findRevision(final SCMSource source, final SCMHead head) {
        try {
            return Optional.ofNullable(source.fetch(head, null));
        }
        catch (IOException | InterruptedException e) {
            throw new IllegalStateException(String.format("Could not fetch revision from repository: %s and branch: %s",
                    source.getId(), head.getName()), e);
        }
    }
}
