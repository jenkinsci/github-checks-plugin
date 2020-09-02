package io.jenkins.plugins.checks.github;

import java.util.Optional;

import edu.hm.hafner.util.FilteredLog;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import org.junit.jupiter.api.Test;

import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Job;
import hudson.model.Run;

class GitHubSCMSourceChecksContextTest {
    private static final String URL = "URL";

    @Test
    void shouldGetHeadShaFromMasterBranch() {
        Job job = mock(Job.class);
        SCMHead head = mock(SCMHead.class);
        AbstractGitSCMSource.SCMRevisionImpl revision = mock(AbstractGitSCMSource.SCMRevisionImpl.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        assertThat(new GitHubSCMSourceChecksContext(job, URL,
                createGitHubSCMFacadeWithRevision(job, source, head, revision, "a1b2c3"))
                .getHeadSha())
                .isEqualTo("a1b2c3");
    }

    @Test
    void shouldGetHeadShaFromPullRequest() {
        Job job = mock(Job.class);
        SCMHead head = mock(SCMHead.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        assertThat(new GitHubSCMSourceChecksContext(job, URL,
                createGitHubSCMFacadeWithRevision(job, source, head, revision, "a1b2c3"))
                .getHeadSha())
                .isEqualTo("a1b2c3");
    }

    @Test
    void shouldGetHeadShaFromRun() {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(run.getParent()).thenReturn(job);
        when(job.getLastBuild()).thenReturn(run);

        assertThat(new GitHubSCMSourceChecksContext(run, URL,
                createGitHubSCMFacadeWithRevision(run, source, revision, "a1b2c3"))
                .getHeadSha())
                .isEqualTo("a1b2c3");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetHeadShaButNoSCMHeadAvailable() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithSource(job, source))
                ::getHeadSha)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No SHA found for job: github-checks-plugin");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetHeadShaButNoSCMRevisionAvailable() {
        Job job = mock(Job.class);
        SCMHead head = mock(SCMHead.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(job.getName()).thenReturn("github-checks-plugin");
        when(source.getRepoOwner()).thenReturn("jenkinsci");
        when(source.getRepository()).thenReturn("github-checks-plugin");
        when(head.getName()).thenReturn("master");

        assertThatThrownBy(new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithRevision(job, source,
                head, null, null))::getHeadSha)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No SHA found for job: github-checks-plugin");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetHeadShaButNoSuitableSCMRevisionAvailable() {
        Job job = mock(Job.class);
        SCMHead head = mock(SCMHead.class);
        SCMRevision revision = mock(SCMRevision.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithRevision(job, source,
                head, revision, null))::getHeadSha)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No SHA found for job: github-checks-plugin");
    }

    @Test
    void shouldGetRepositoryName() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(source.getRepoOwner()).thenReturn("jenkinsci");
        when(source.getRepository()).thenReturn("github-checks-plugin");

        assertThat(new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithSource(job, source)).getRepository())
                .isEqualTo("jenkinsci/github-checks-plugin");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetRepositoryButNoGitHubSCMSourceAvailable() {
        Job job = mock(Job.class);
        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(() -> new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithSource(job, null))
                .getRepository())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No GitHub SCM source found for job: github-checks-plugin");
    }

    @Test
    void shouldGetCredentials() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);

        assertThat(new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithCredentials(job, source, credentials, "1"))
                .getCredentials())
                .isEqualTo(credentials);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetCredentialsButNoCredentialsAvailable() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithCredentials(job, source,
                null, null))::getCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No GitHub APP credentials available for job: github-checks-plugin");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetCredentialsButNoSourceAvailable() {
        Job job = mock(Job.class);
        SCMFacade scmFacade = mock(SCMFacade.class);

        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(new GitHubSCMSourceChecksContext(job, URL, scmFacade)::getCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No GitHub APP credentials available for job: github-checks-plugin");
    }

    @Test
    void shouldGetURLForJob() {
        Job job = mock(Job.class);

        assertThat(new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithSource(job, null)).getURL())
                .isEqualTo(URL);
    }

    @Test
    void shouldGetURLForRun() {
        Run<?, ?> run = mock(Run.class);
        Job<?, ?> job = mock(Job.class);
        ClassicDisplayURLProvider urlProvider = mock(ClassicDisplayURLProvider.class);

        when(urlProvider.getRunURL(run))
                .thenReturn("http://127.0.0.1:8080/job/github-checks-plugin/job/master/200");

        assertThat(new GitHubSCMSourceChecksContext(run, urlProvider.getRunURL(run),
                createGitHubSCMFacadeWithSource(job, null)).getURL())
                .isEqualTo("http://127.0.0.1:8080/job/github-checks-plugin/job/master/200");
    }

    @Test
    void shouldReturnFalseWhenValidateContextButHasNoValidCredentials() {
        Job<?, ?> job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        FilteredLog logger = new FilteredLog("");

        assertThat(new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithSource(job, source))
                .isValid(logger))
                .isFalse();
        assertThat(logger.getErrorMessages()).contains("No credentials found");
    }

    @Test
    void shouldReturnFalseWhenValidateContextButHasNoValidGitHubAppCredentials() {
        Job<?, ?> job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        FilteredLog logger = new FilteredLog("");

        when(source.getCredentialsId()).thenReturn("oauth-credentials");

        assertThat(new GitHubSCMSourceChecksContext(job, URL, createGitHubSCMFacadeWithSource(job, source))
                .isValid(logger))
                .isFalse();
        assertThat(logger.getErrorMessages())
                .contains("No GitHub app credentials found: 'oauth-credentials'")
                .contains("See: https://github.com/jenkinsci/github-branch-source-plugin/blob/master/docs/github-app.adoc");
    }

    @Test
    void shouldReturnFalseWhenValidateContextButHasNoValidSHA() {
        Run run = mock(Run.class);
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);
        FilteredLog logger = new FilteredLog("");

        when(run.getParent()).thenReturn(job);

        when(source.getRepoOwner()).thenReturn("jenkinsci");
        when(source.getRepository()).thenReturn("github-checks");

        assertThat(new GitHubSCMSourceChecksContext(run, URL, createGitHubSCMFacadeWithCredentials(job, source,
                credentials, "1")).isValid(logger))
                .isFalse();
        assertThat(logger.getErrorMessages()).contains("No HEAD SHA found for jenkinsci/github-checks");
    }

    private SCMFacade createGitHubSCMFacadeWithRevision(final Job<?, ?> job, final GitHubSCMSource source,
                                                        final SCMHead head, final SCMRevision revision,
                                                        final String hash) {
        SCMFacade facade = createGitHubSCMFacadeWithSource(job, source);

        when(facade.findHead(job)).thenReturn(Optional.ofNullable(head));
        when(facade.findRevision(source, head)).thenReturn(Optional.ofNullable(revision));
        when(facade.findHash(revision)).thenReturn(Optional.ofNullable(hash));

        return facade;
    }

    private SCMFacade createGitHubSCMFacadeWithRevision(final Run<?, ?> run, final GitHubSCMSource source,
                                                        final SCMRevision revision, final String hash) {
        SCMFacade facade = createGitHubSCMFacadeWithSource(run.getParent(), source);

        when(facade.findRevision(source, run)).thenReturn(Optional.of(revision));
        when(facade.findHash(revision)).thenReturn(Optional.of(hash));

        return facade;
    }

    private SCMFacade createGitHubSCMFacadeWithCredentials(final Job<?, ?> job, final GitHubSCMSource source,
                                                           final GitHubAppCredentials credentials,
                                                           final String credentialsId) {
        SCMFacade facade = createGitHubSCMFacadeWithSource(job, source);

        when(source.getCredentialsId()).thenReturn(credentialsId);
        when(facade.findGitHubAppCredentials(job, credentialsId)).thenReturn(Optional.ofNullable(credentials));

        return facade;
    }

    private SCMFacade createGitHubSCMFacadeWithSource(final Job<?, ?> job, final GitHubSCMSource source) {
        SCMFacade facade = mock(SCMFacade.class);

        when(facade.findGitHubSCMSource(job)).thenReturn(Optional.ofNullable(source));

        return facade;
    }
}
