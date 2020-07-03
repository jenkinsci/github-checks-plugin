package io.jenkins.plugins.checks.github;

import java.io.IOException;

import com.cloudbees.plugins.credentials.CredentialsProvider;

import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import hudson.model.Run;
import jenkins.plugins.git.AbstractGitSCMSource.SCMRevisionImpl;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

class GitHubContextResolver {
    /**
     * Resolves the {@link GitHubSCMSource} from the run.
     *
     * @param run
     *         a run of a GitHub Branch Source project
     * @return the resolved source
     */
    public GitHubSCMSource resolveSource(final Run<?, ?> run) {
        SCMSource source = SCMSource.SourceByItem.findSource(run.getParent());
        if (source == null) {
            throw new IllegalStateException("Could not resolve source from run: " + run);
        }
        else if (source instanceof GitHubSCMSource) {
            return (GitHubSCMSource) source;
        }
        else {
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

    public String resolveHeadSha(final SCMSource source, final Run<?, ?> run) {
        SCMHead head = resolveHead(run);
        SCMRevision revision = resolveRevision(source, head);
        return resolveHeadSha(revision);
    }

    private SCMHead resolveHead(final Run<?, ?> run) {
        SCMHead head = SCMHead.HeadByItem.findHead(run.getParent());
        if (head == null) {
            throw new IllegalStateException("Could not resolve head from run: " + run);
        }
        return head;
    }

    private SCMRevision resolveRevision(final SCMSource source, final SCMHead head) {
        SCMRevision revision;
        try {
            revision = source.fetch(head, null);
        }
        catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Could not resolve head sha: ", e);
        }

        if (revision == null) {
            throw new IllegalStateException(String.format(
                    "Could not resolve revision, source: %s, head: %s", source, head));
        }

        return revision;
    }

    private String resolveHeadSha(final SCMRevision revision) {
        if (revision instanceof SCMRevisionImpl) {
            return ((SCMRevisionImpl) revision).getHash();
        }
        else if (revision instanceof PullRequestSCMRevision) {
            return ((PullRequestSCMRevision) revision).getPullHash();
        }
        else {
            throw new IllegalStateException("Could not resolve head sha from revision type: "
                    + revision.getClass().getName());
        }
    }
}
