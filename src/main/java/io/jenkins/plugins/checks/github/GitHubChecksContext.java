package io.jenkins.plugins.checks.github;

import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import hudson.model.Run;
import jenkins.scm.api.SCMSource;

class GitHubChecksContext {
    private final GitHubSCMSource source;
    private final GitHubAppCredentials credentials;
    private final String headSha;
    private final String url;

    /**
     * Creates a {@link GitHubChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param run
     *         a run of a GitHub Branch Source project
     */
    GitHubChecksContext(final Run<?, ?> run) {
        GitHubContextResolver resolver = new GitHubContextResolver();
        this.source = resolver.resolveSource(run);
        this.credentials = resolver.resolveCredentials(source, run);
        this.headSha = resolver.resolveHeadSha(source, run);
        this.url = run.getParent().getAbsoluteUrl() + run.getNumber() + "/";
    }

    /**
     * Returns the {@link SCMSource} of the run.
     *
     * @return {@link SCMSource} of the run or null
     */
    public SCMSource getSource() {
        return source;
    }

    /**
     * Returns the commit sha of the run.
     *
     * @return the commit sha of the run or null
     */
    public String getHeadSha() {
        return headSha;
    }

    /**
     * Returns the source repository's full name of the run. The full name consists of the owner's name and the
     * repository's name, e.g. jenkins-ci/jenkins
     *
     * @return the source repository's full name
     */
    public String getRepository() {
        return source.getRepoOwner() + "/" + source.getRepository();
    }

    /**
     * Returns the credentials to access the remote GitHub repository.
     *
     * @return the credentials or null
     */
    public GitHubAppCredentials getCredentials() {
        return credentials;
    }

    /**
     * Returns the URL of the run's summary page, e.g. https://ci.jenkins.io/job/Core/job/jenkins/job/master/2000/.
     *
     * @return the URL of the summary page
     */
    public String getURL() {
        return url;
    }
}
