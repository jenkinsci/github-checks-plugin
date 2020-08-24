package io.jenkins.plugins.checks.github;

import java.util.Optional;

import edu.hm.hafner.util.FilteredLog;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import edu.umd.cs.findbugs.annotations.Nullable;

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
     * Returns whether the context is valid (with all properties functional) to use.
     *
     * @param logger
     *         the filtered logger
     * @return whether the context is valid to use
     */
    public abstract boolean isValid(FilteredLog logger);

    @Nullable
    protected abstract String getCredentialsId();

    /**
     * Returns the credentials to access the remote GitHub repository.
     *
     * @return the credentials or null
     */
    public GitHubAppCredentials getCredentials() {
        String credentialsId = getCredentialsId();
        return getGitHubAppCredentials(credentialsId);
    }

    /**
     * Returns the URL of the run's summary page, e.g. https://ci.jenkins.io/job/Core/job/jenkins/job/master/2000/.
     *
     * @return the URL of the summary page
     */
    public String getURL() {
        return url;
    }

    protected Job<?, ?> getJob() {
        return job;
    }

    protected SCMFacade getScmFacade() {
        return scmFacade;
    }

    protected GitHubAppCredentials getGitHubAppCredentials(final String credentialsId) {
        Optional<GitHubAppCredentials> foundCredentials = findGitHubAppCredentials(credentialsId);
        if (!foundCredentials.isPresent()) {
            throw new IllegalStateException("No GitHub APP credentials available for job: " + getJob().getName());
        }

        return foundCredentials.get();
    }

    protected boolean hasGitHubAppCredentials() {
        return findGitHubAppCredentials(getCredentialsId()).isPresent();
    }

    protected boolean hasCredentialsId() {
        return StringUtils.isNoneBlank(getCredentialsId());
    }

    protected boolean hasValidCredentials(final FilteredLog logger) {
        if (!hasCredentialsId()) {
            logger.logError("No credentials found");

            return false;
        }

        if (!hasGitHubAppCredentials()) {
            logger.logError("No GitHub app credentials found: '%s'", getCredentialsId());
            logger.logError("See: https://github.com/jenkinsci/github-branch-source-plugin/blob/master/docs/github-app.adoc");

            return false;
        }
        
        return true;
    }

    private Optional<GitHubAppCredentials> findGitHubAppCredentials(final String credentialsId) {
        return getScmFacade().findGitHubAppCredentials(getJob(), credentialsId);
    }
}
