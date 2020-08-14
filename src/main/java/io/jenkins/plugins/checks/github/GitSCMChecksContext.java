package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;

/**
 * Provides check properties that should be resolved  Jenkins job.
 */
class GitSCMChecksContext extends GitHubChecksContext {
    /**
     * Creates a {@link GitSCMChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param run a run of a GitHub Branch Source project
     */
    GitSCMChecksContext(final Run<?, ?> run) {
        super(run.getParent(), run, new GitHubSCMFacade(), DisplayURLProvider.get());
    }

    @Override
    public String getHeadSha() {
        try {
            return getRun().getEnvironment(TaskListener.NULL).get("GIT_COMMIT");
        }
        catch (IOException | InterruptedException e) {
            // ignore and return a default
        }
        return "HEAD"; 
    }

    // TODO: check which other kind of repository strings are valid
    @Override
    public String getRepository() {
        UserRemoteConfig config = getUserRemoteConfig();
        String withoutProtocol = StringUtils.removeStart(
                StringUtils.removeStart(config.getUrl(), "git@github.com:"), 
                "https://github.com/");
        return StringUtils.removeEnd(withoutProtocol, ".git");
    }

    private UserRemoteConfig getUserRemoteConfig() {
        List<UserRemoteConfig> configs = resolveGitSCM().getUserRemoteConfigs();
        if (configs.isEmpty()) {
            return new UserRemoteConfig(null, null, null, null);
        }
        return configs.get(0);
    }

    @Override
    protected String getCredentialsId() {
        return getUserRemoteConfig().getCredentialsId();
    }

    private GitSCM resolveGitSCM() {
        SCM scm = new ScmResolver().getScm(getRun());

        if (scm instanceof GitSCM) {
            return (GitSCM) scm;
        }
        throw new IllegalStateException("No Git SCM source available for job: " + getJob().getName());
    }
}