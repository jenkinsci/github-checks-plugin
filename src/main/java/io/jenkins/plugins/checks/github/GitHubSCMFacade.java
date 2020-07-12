package io.jenkins.plugins.checks.github;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Run;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import java.util.Optional;

class GitHubSCMFacade {
    /**
     * Returns the {@link GitHubSCMSource} used in the {@code run}.
     *
     * @param run
     *         a Jenkins run
     *
     * @return the GitHub SCM source if exists
     */
    Optional<GitHubSCMSource> findGitHubSCMSource(final Run<?, ?> run) {
        SCMSource source = SCMSource.SourceByItem.findSource(run.getParent());
        return source instanceof GitHubSCMSource ? Optional.of((GitHubSCMSource) source) : Optional.empty();
    }

    /**
     * Returns {@link GitHubAppCredentials} configured in the run.
     *
     * @param run
     *         a Jenkins run
     * @param credentialsId
     *         the id of credentials
     *
     * @return the GitHub app credentials if configured
     */
    Optional<GitHubAppCredentials> findGitHubAppCredentials(final Run<?, ?> run, final String credentialsId) {
        return Optional.ofNullable(
                CredentialsProvider.findCredentialById(credentialsId, GitHubAppCredentials.class, run));
    }
}
