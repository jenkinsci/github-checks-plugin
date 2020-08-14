package io.jenkins.plugins.checks.github;

import java.util.Optional;

import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import edu.umd.cs.findbugs.annotations.Nullable;

import hudson.model.Job;
import hudson.model.Run;

/**
 * Base class for a context that publishes GitHub checks.
 */
abstract class GitHubChecksContext {
    @Nullable // FIXME: why is this nullable?
    private final Run<?, ?> run;
    private final Job<?, ?> job;
    private final DisplayURLProvider urlProvider;
    private final GitHubSCMFacade scmFacade;

    GitHubChecksContext(final Job<?, ?> job, @Nullable final Run<?, ?> run,
                        final GitHubSCMFacade scmFacade, final DisplayURLProvider urlProvider) {
        this.job = job;
        this.run = run;
        this.scmFacade = scmFacade;
        this.urlProvider = urlProvider;
    }

    /**
     * Returns the Jenkins job.
     *
     * @return job for which the checks will be based on
     */
    public Job<?, ?> getJob() {
        return job;
    }

    @Nullable
    public Run<?, ?> getRun() {
        return run;
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

    protected abstract String getCredentialsId();

    /**
     * Returns the URL of the run's summary page, e.g. https://ci.jenkins.io/job/Core/job/jenkins/job/master/2000/.
     *
     * @return the URL of the summary page
     */
    public String getURL() {
        if (run == null) {
            return urlProvider.getJobURL(job);
        }
        else {
            return urlProvider.getRunURL(run);
        }
    }

    GitHubSCMFacade getScmFacade() {
        return scmFacade;
    }

    protected GitHubAppCredentials getGitHubAppCredentials(final String credentialsId) {
        Optional<GitHubAppCredentials> foundCredentials
                = getScmFacade().findGitHubAppCredentials(getJob(), credentialsId);
        if (!foundCredentials.isPresent()) {
            throw new IllegalStateException("No GitHub APP credentials available for job: " + getJob().getName());
        }

        return foundCredentials.get();
    }
}
