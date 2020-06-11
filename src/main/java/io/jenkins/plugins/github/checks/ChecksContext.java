package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import hudson.model.Run;
import hudson.util.Secret;
import jenkins.plugins.git.AbstractGitSCMSource.SCMRevisionImpl;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

// TODO: Refactor this class
public class ChecksContext {
    private String repository;
    private String headSha;
    private String token;

    private static final Logger LOGGER = Logger.getLogger(ChecksContext.class.getName());

    private final GitHubSCMSource source;
    private final SCMHead head;
    private final Run<?, ?> run;

    public ChecksContext(Run<?, ?> run) {
        this.run = run;
        source = resolveSource(run);
        head = resolveHead(run);
    }

    @CheckForNull
    public String getRepository() {
        if (repository == null) {
            repository = source.getRepoOwner() + "/" + source.getRepository();
        }
        return repository;
    }

    @CheckForNull
    public String getHeadSha() {
        if (headSha == null) {
            try {
                headSha = resolveHeadCommit(source.fetch(head, null));
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.FINE, "Could not resolve head sha. Message: " + e.getMessage());
                return null;
            }
        }
        return headSha;
    }

    @CheckForNull
    public StandardCredentials getCrendential() {
        GitHubAppCredentials credentials = null;
        if (source.getCredentialsId() != null) {
            credentials = CredentialsProvider
                    .findCredentialById(source.getCredentialsId(), GitHubAppCredentials.class, run);
        }
        return credentials;
    }

    @CheckForNull
    public String getToken() {
        if (token == null) {
            token = resolveToken(source, run);
        }
        return token;
    }

    @CheckForNull
    public SCMSource getSource() {
        return source;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    @CheckForNull
    private static GitHubSCMSource resolveSource(Run<?, ?> run) {
        return (GitHubSCMSource) SCMSource.SourceByItem.findSource(run.getParent());
    }

    @CheckForNull
    private static SCMHead resolveHead(Run<?, ?> run) {
        return SCMHead.HeadByItem.findHead(run.getParent());
    }

    @CheckForNull
    private static String resolveHeadCommit(SCMRevision revision) throws IllegalArgumentException {
        if (revision instanceof SCMRevisionImpl) {
            return ((SCMRevisionImpl) revision).getHash();
        } else if (revision instanceof PullRequestSCMRevision) {
            return ((PullRequestSCMRevision) revision).getPullHash();
        } else {
            return null;
        }
    }

    @CheckForNull
    private static String resolveToken(GitHubSCMSource source, Run<?, ?> run) {
        if (source.getCredentialsId() != null) {
            GitHubAppCredentials appCredentials = CredentialsProvider
                    .findCredentialById(source.getCredentialsId(), GitHubAppCredentials.class, run);
            if (appCredentials != null) {
                return Secret.toString(appCredentials.getPassword());
            }
        }
        return null;
    }
}
