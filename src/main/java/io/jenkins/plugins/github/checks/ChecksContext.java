package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cloudbees.plugins.credentials.CredentialsProvider;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import hudson.model.Run;
import jenkins.scm.api.SCMSource;

// TODO: Refactor this class
public class ChecksContext {
    private final Run<?, ?> run;
    private final GitHubSCMSource source;
    private final String headSha;

    private static final Logger LOGGER = Logger.getLogger(ChecksContext.class.toString());

    public ChecksContext(Run<?, ?> run) {
        this(run, new ContextResolver());
    }

    @VisibleForTesting
    ChecksContext(Run<?, ?> run, ContextResolver resolver) {
        this.run = run;
        this.source = resolver.resolveSource(run);
        String headSha = null;
        try {
            headSha = resolver.resolveHeadSha(source, run);
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Could not resolve head sha. Message:", e);
        }
        this.headSha = headSha;
    }

    @CheckForNull
    public SCMSource getSource() {
        return source;
    }

    @CheckForNull
    public String getHeadSha() {
        return headSha;
    }

    @CheckForNull
    public String getRepository() {
        return source.getRepoOwner() + "/" + source.getRepository();
    }

    @CheckForNull
    public GitHubAppCredentials getCredential() {
        if (source.getCredentialsId() != null) {
            return CredentialsProvider.findCredentialById(source.getCredentialsId(), GitHubAppCredentials.class, run);
        }
        return null;
    }

    public String getURL() {
        return run.getParent().getAbsoluteUrl() + run.getNumber() + "/";
    }
}
