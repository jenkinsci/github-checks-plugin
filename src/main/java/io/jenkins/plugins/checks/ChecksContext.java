package io.jenkins.plugins.checks;

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

public class ChecksContext {
    private final Run<?, ?> run;
    private final GitHubSCMSource source;
    private final String headSha;

    private static final Logger LOGGER = Logger.getLogger(ChecksContext.class.toString());

    /**
     * Creates a context for the run, including the {@link SCMSource} and commit sha.
     *
     * @param run
     *         the run which needs to be resolved
     */
    public ChecksContext(final Run<?, ?> run) {
        this(run, new ContextResolver());
    }

    @VisibleForTesting
    ChecksContext(final Run<?, ?> run, final ContextResolver resolver) {
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

    /**
     * Returns the {@link SCMSource} of the run.
     *
     * @return {@link SCMSource} of the run or null
     */
    @CheckForNull
    public SCMSource getSource() {
        return source;
    }

    /**
     * Returns the commit sha of the run.
     *
     * @return the commit sha of the run or null
     */
    @CheckForNull
    public String getHeadSha() {
        return headSha;
    }

    /**
     * Returns the source repository's full name of the run. The full name consists of the owner's name and the
     * repository's name, e.g. jenkins-ci/jenkins
     *
     * @return the source repository's full name
     */
    @CheckForNull
    public String getRepository() {
        return source.getRepoOwner() + "/" + source.getRepository();
    }

    /**
     * Returns the credential to access the remote repository. The credential is resolved according to the
     * {@link SCMSource} of the run.
     *
     * @return the credential or null
     */
    @CheckForNull
    public GitHubAppCredentials getCredential() {
        if (source.getCredentialsId() != null) {
            return CredentialsProvider.findCredentialById(source.getCredentialsId(), GitHubAppCredentials.class, run);
        }
        return null;
    }

    /**
     * Returns the URL of the run's summary page, e.g. https://ci.jenkins.io/job/Core/job/jenkins/job/master/2000/.
     *
     * @return the URL of the summary page
     */
    public String getURL() {
        return run.getParent().getAbsoluteUrl() + run.getNumber() + "/";
    }
}
