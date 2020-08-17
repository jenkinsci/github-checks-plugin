package io.jenkins.plugins.checks.github;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.jenkins.plugins.util.PluginLogger;

import hudson.model.Job;

/**
 * Base class for a context that publishes GitHub checks.
 */
abstract class GitHubChecksContext {
    private final Job<?, ?> job;
    private final String url;
    private final SCMFacade scmFacade;

    GitHubChecksContext(final Job<?, ?> job, final String url, final SCMFacade scmFacade) {
        this.job = job;
        this.url = url;
        this.scmFacade = scmFacade;
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
    public abstract String getHeadSha();

    /**
     * Returns the source repository's full name of the run. The full name consists of the owner's name and the
     * repository's name, e.g. jenkins-ci/jenkins
     *
     * @return the source repository's full name
     */
    public abstract String getRepository();

    /**
     * Returns the credentials to access the remote GitHub repository.
     *
     * @return the credentials or null
     */
    public GitHubAppCredentials getCredentials() {
        String credentialsId = getCredentialsId();
        if (credentialsId == null) {
            throw new IllegalStateException("No credentials available for job: " + getJob().getName());
        }

        return getGitHubAppCredentials(credentialsId);
    }

    @Nullable
    protected abstract String getCredentialsId();

    /**
     * Returns the URL of the run's summary page, e.g. https://ci.jenkins.io/job/Core/job/jenkins/job/master/2000/.
     *
     * @return the URL of the summary page
     */
    public String getURL() {
        return url;
    }

    SCMFacade getScmFacade() {
        return scmFacade;
    }

    protected GitHubAppCredentials getGitHubAppCredentials(final String credentialsId) {
        Optional<GitHubAppCredentials> foundCredentials = findGitHubAppCredentials(credentialsId);
        if (!foundCredentials.isPresent()) {
            throw new IllegalStateException("No GitHub APP credentials available for job: " + getJob().getName());
        }

        return foundCredentials.get();
    }

    Optional<GitHubAppCredentials> findGitHubAppCredentials(final String credentialsId) {
        return getScmFacade().findGitHubAppCredentials(getJob(), credentialsId);
    }

    abstract boolean isValid(PluginLogger listener);

    protected boolean hasGitHubAppCredentials() {
        return findGitHubAppCredentials(getCredentialsId()).isPresent();
    }

    protected boolean hasCredentialsId() {
        return StringUtils.isNoneBlank(getCredentialsId());
    }

    protected boolean hasValidCredentials(final PluginLogger logger) {
        if (!hasCredentialsId()) {
            logger.log("No credentials found");

            return false;
        }

        if (!hasGitHubAppCredentials()) {
            logger.log("No GitHub app credentials found: '%s'", getCredentialsId());
            logger.log("See: https://github.com/jenkinsci/github-branch-source-plugin/blob/master/docs/github-app.adoc");

            return false;
        }
        
        return true;
    }
}
