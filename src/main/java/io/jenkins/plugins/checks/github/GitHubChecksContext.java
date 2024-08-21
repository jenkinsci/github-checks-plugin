package io.jenkins.plugins.checks.github;

import java.util.Optional;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.model.Job;
import hudson.model.Run;

/**
 * Base class for a context that publishes GitHub checks.
 */
public abstract class GitHubChecksContext {
    private final Job<?, ?> job;
    private final String url;
    private final SCMFacade scmFacade;

    protected GitHubChecksContext(final Job<?, ?> job, final String url, final SCMFacade scmFacade) {
        this.job = job;
        this.url = url;
        this.scmFacade = scmFacade;
    }

    /**
     * Returns the commit sha of the run.
     *
     * @return the commit sha of the run
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

    @CheckForNull
    protected abstract String getCredentialsId();

    /**
     * Returns the credentials to access the remote GitHub repository.
     *
     * @return the credentials
     */
    public StandardUsernameCredentials getCredentials() {
        return getGitHubAppCredentials(StringUtils.defaultIfEmpty(getCredentialsId(), ""));
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

    protected final SCMFacade getScmFacade() {
        return scmFacade;
    }

    protected StandardUsernameCredentials getGitHubAppCredentials(final String credentialsId) {
        return findGitHubAppCredentials(credentialsId).orElseThrow(() ->
                new IllegalStateException("No GitHub APP credentials available for job: " + getJob().getName()));
    }

    protected boolean hasGitHubAppCredentials() {
        return findGitHubAppCredentials(StringUtils.defaultIfEmpty(getCredentialsId(), "")).isPresent();
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

    private Optional<StandardUsernameCredentials> findGitHubAppCredentials(final String credentialsId) {
        return getScmFacade().findGitHubAppCredentials(getJob(), credentialsId);
    }

    /**
     * Returns the id of a {@link GitHubChecksAction} for this run, if any.
     *
     * @param name
     *         the name of the check
     * @return the id of the check run
     */
    public Optional<Long> getId(final String name) {
        return getAction(name).map(GitHubChecksAction::getId);
    }

    protected abstract Optional<Run<?, ?>> getRun();

    private Optional<GitHubChecksAction> getAction(final String name) {
        if (!getRun().isPresent()) {
            return Optional.empty();
        }
        return getRun().get().getActions(GitHubChecksAction.class)
                .stream()
                .filter(a -> a.getName().equals(name))
                .findFirst();
    }

    void addActionIfMissing(final long id, final String name) {
        if (!getRun().isPresent()) {
            return;
        }
        Optional<GitHubChecksAction> action = getAction(name);
        if (!action.isPresent()) {
            getRun().get().addAction(new GitHubChecksAction(id, name));
        }
    }
}
