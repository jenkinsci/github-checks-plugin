package io.jenkins.plugins.checks.github;

import com.cloudbees.plugins.credentials.CredentialsProvider;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import hudson.model.Run;
import jenkins.scm.api.SCMSource;

import io.jenkins.plugins.checks.ContextResolver;

class GitHubContextResolver extends ContextResolver {
    /**
     * Resolves the {@link GitHubSCMSource} from the run.
     *
     * @param run
     *         a run of a GitHub Branch Source project
     * @return the resolved source
     */
    public GitHubSCMSource resolveSource(final Run<?, ?> run) {
        SCMSource source = super.resolveSource(run);
        if (source instanceof GitHubSCMSource) {
            return (GitHubSCMSource) source;
        } else {
            throw new IllegalStateException("The scm source of the run is not an instance of GitHubSCMSource: "
                    + source.getClass().getName());
        }
    }

    /**
     * Resolves the GitHub APP credentials.
     *
     * @param source
     *         a {@link GitHubSCMSource}
     * @param run
     *         a run of a GitHub Branch Source project
     * @return the resolved credentials
     */
    public GitHubAppCredentials resolveCredentials(final GitHubSCMSource source, final Run<?, ?> run) {
        String credentialsId = source.getCredentialsId();
        if (credentialsId == null) {
            throw new IllegalStateException("Could not resolve credentials when access GitHub anonymously");
        }

        GitHubAppCredentials credentials =
                CredentialsProvider.findCredentialById(credentialsId, GitHubAppCredentials.class, run);
        if (credentials == null) {
            throw new IllegalStateException(String.format("Could not resolve GitHub APP credentials,"
                    + "source: %s, run %s", source.getRepository(), run));
        }

        return credentials;
    }
}
