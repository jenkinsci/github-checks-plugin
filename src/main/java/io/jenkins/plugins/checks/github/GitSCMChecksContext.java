package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.util.Optional;

import edu.hm.hafner.util.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.jenkins.plugins.util.PluginLogger;

import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.Revision;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.util.BuildData;

/**
 * Provides a {@link GitHubChecksContext} for a Jenkins job that uses a supported {@link GitSCM}.
 */
class GitSCMChecksContext extends GitHubChecksContext {
    private static final String GIT_PROTOCOL = "git@github.com:";
    private static final String HTTPS_PROTOCOL = "https://github.com/";

    private final Run<?, ?> run;

    /**
     * Creates a {@link GitSCMChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param run    a run of a GitHub Branch Source project
     * @param runURL the URL to the Jenkins run
     */
    GitSCMChecksContext(final Run<?, ?> run, final String runURL) {
        this(run, runURL, new SCMFacade());
    }

    @VisibleForTesting
    GitSCMChecksContext(final Run<?, ?> run, final String runURL, final SCMFacade scmFacade) {
        super(run.getParent(), runURL, scmFacade);

        this.run = run;
    }

    @Override
    public String getHeadSha() {
        try {
            String head = getGitCommitEnvironment();
            if (StringUtils.isNotBlank(head)) {
                return head;
            }
            return getLastBuiltRevisionFromBuildData();
        }
        catch (IOException | InterruptedException e) {
            // ignore and return a default
        }
        return StringUtils.EMPTY;
    }

    public String getGitCommitEnvironment() throws IOException, InterruptedException {
        return StringUtils.defaultString(run.getEnvironment(TaskListener.NULL).get("GIT_COMMIT"));
    }

    private String getLastBuiltRevisionFromBuildData() {
        BuildData gitBuildData = run.getAction(BuildData.class);
        if (gitBuildData != null) {
            Revision lastBuiltRevision = gitBuildData.getLastBuiltRevision();
            if (lastBuiltRevision != null) {
                return lastBuiltRevision.getSha1().getName();
            }
        }
        return StringUtils.EMPTY;
    }

    // TODO: check which other kind of repository strings are valid
    @Override
    public String getRepository() {
        return StringUtils.removeEnd(removeProtocol(getRemoteUrl()), ".git");
    }

    @Nullable
    private String getRemoteUrl() {
        return getUserRemoteConfig().getUrl();
    }

    private String removeProtocol(final String url) {
        return StringUtils.removeStart(StringUtils.removeStart(url, GIT_PROTOCOL), HTTPS_PROTOCOL);
    }

    @Override @Nullable
    protected String getCredentialsId() {
        return getUserRemoteConfig().getCredentialsId();
    }

    private UserRemoteConfig getUserRemoteConfig() {
        return getScmFacade().getUserRemoteConfig(resolveGitSCM());
    }

    private GitSCM resolveGitSCM() {
        Optional<GitSCM> gitSCM = getScmFacade().findGitSCM(run);
        if (gitSCM.isPresent()) {
            return gitSCM.get();
        }
        throw new IllegalStateException(
                "Skipped publishing GitHub checks: no Git SCM source available for job: " + getJob().getName());
    }

    @Override
    public boolean isValid(final PluginLogger logger) {
        if (!getScmFacade().findGitSCM(run).isPresent()) {
            logger.log("Job does not use GitSCM");

            return false;
        }

        String remoteUrl = getRemoteUrl();
        if (!isValidUrl(remoteUrl)) {
            logger.log("No supported GitSCM repository URL: " + remoteUrl);

            return false;
        }

        if (!hasValidCredentials(logger)) {
            return false;
        }

        String repository = getRepository();
        if (getHeadSha().isEmpty()) {
            logger.log("No HEAD SHA found for '%s'", repository);

            return false;
        }

        logger.log("Using GitSCM repository '%s' for GitHub checks", repository);

        return true;
    }

    private boolean isValidUrl(@Nullable final String remoteUrl) {
        return StringUtils.startsWith(remoteUrl, GIT_PROTOCOL) 
                || StringUtils.startsWith(remoteUrl, HTTPS_PROTOCOL);
    }
}