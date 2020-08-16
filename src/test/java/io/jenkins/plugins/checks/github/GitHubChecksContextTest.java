package io.jenkins.plugins.checks.github;

import hudson.model.Job;
import hudson.model.Run;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitHubChecksContextTest {
    @Test
    void shouldGetHeadShaWhenResolveRevisionFromRun() {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(run.getParent()).thenReturn(job);
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(scmFacade.findRevision(source, run)).thenReturn(Optional.of(revision));
        when(revision.getPullHash()).thenReturn("a1b2c3");

        assertThat(new GitHubChecksContext(run, scmFacade, null)
                .getHeadSha())
                .isEqualTo("a1b2c3");
    }

    @Test
    void shouldGetHeadShaWhenResolveRevisionFromHead() {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        SCMHead head = mock(SCMHead.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubSCMFacade scmFacade = createGitHubSCMFacadeWithRevision(job, source, head, revision);

        when(run.getParent()).thenReturn(job);
        when(revision.getPullHash()).thenReturn("a1b2c3");

        assertThat(new GitHubChecksContext(run, scmFacade, null)
                .getHeadSha())
                .isEqualTo("a1b2c3");
    }

    @Test
    void shouldGetHeadShaFromMasterBranch() {
        Job job = mock(Job.class);
        SCMHead head = mock(SCMHead.class);
        AbstractGitSCMSource.SCMRevisionImpl revision = mock(AbstractGitSCMSource.SCMRevisionImpl.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(revision.getHash()).thenReturn("a1b2c3");

        assertThat(new GitHubChecksContext(job, createGitHubSCMFacadeWithRevision(job, source, head, revision), null)
                .getHeadSha())
                .isEqualTo("a1b2c3");
    }

    @Test
    void shouldGetHeadShaFromPullRequest() {
        Job job = mock(Job.class);
        SCMHead head = mock(SCMHead.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(revision.getPullHash()).thenReturn("a1b2c3");

        assertThat(new GitHubChecksContext(job, createGitHubSCMFacadeWithRevision(job, source, head, revision), null)
                .getHeadSha())
                .isEqualTo("a1b2c3");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetHeadShaButNoSCMHeadAvailable() {
        Job job = mock(Job.class);
        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(() -> new GitHubChecksContext(job).getHeadSha())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No SCM head available for job: github-checks-plugin");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetHeadShaButNoSCMRevisionAvailable() {
        Job job = mock(Job.class);
        SCMHead head = mock(SCMHead.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(source.getRepoOwner()).thenReturn("jenkinsci");
        when(source.getRepository()).thenReturn("github-checks-plugin");
        when(head.getName()).thenReturn("master");

        assertThatThrownBy(() ->
                new GitHubChecksContext(job, createGitHubSCMFacadeWithRevision(job, source, head, null), null)
                        .getHeadSha())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No SCM revision available for repository: jenkinsci/github-checks-plugin and "
                        + "head: master");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetHeadShaButNoSuitableSCMRevisionAvailable() {
        Job job = mock(Job.class);
        SCMHead head = mock(SCMHead.class);
        SCMRevision revision = mock(SCMRevision.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        assertThatThrownBy(() ->
                new GitHubChecksContext(job, createGitHubSCMFacadeWithRevision(job, source, head, revision), null)
                        .getHeadSha())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unsupported revision type: " + revision.getClass().getName());
    }

    @Test
    void shouldGetRepositoryName() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(source.getRepoOwner()).thenReturn("jenkinsci");
        when(source.getRepository()).thenReturn("github-checks-plugin");

        assertThat(new GitHubChecksContext(job, createGitHubSCMFacadeWithSource(job, source), null).getRepository())
                .isEqualTo("jenkinsci/github-checks-plugin");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetRepositoryButNoGitHubSCMSourceAvailable() {
        Job job = mock(Job.class);
        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(() ->
                new GitHubChecksContext(job, createGitHubSCMFacadeWithSource(job, null), null)
                        .getRepository())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No GitHub SCM source available for job: github-checks-plugin");
    }

    @Test
    void shouldGetCredentials() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);

        assertThat(new GitHubChecksContext(job, createGitHubSCMFacadeWithCredentials(job, source, credentials, "1"), null)
                .getCredentials())
                .isEqualTo(credentials);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetCredentialsButNoCredentialsAvailable() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(() ->
                new GitHubChecksContext(job, createGitHubSCMFacadeWithCredentials(job, source, null, null), null)
                        .getCredentials())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No credentials available for job: github-checks-plugin");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetCredentialsButNoGitHubAPPCredentialsAvailable() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(() ->
                new GitHubChecksContext(job, createGitHubSCMFacadeWithCredentials(job, source, null, "1"), null)
                        .getCredentials())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No GitHub APP credentials available for job: github-checks-plugin");
    }

    @Test
    void shouldGetURLForJob() {
        Job job = mock(Job.class);
        ClassicDisplayURLProvider urlProvider = mock(ClassicDisplayURLProvider.class);

        when(urlProvider.getJobURL(job))
                .thenReturn("http://127.0.0.1:8080/job/github-checks-plugin/job/master/");

        assertThat(new GitHubChecksContext(job, null, urlProvider).getURL())
                .isEqualTo("http://127.0.0.1:8080/job/github-checks-plugin/job/master/");
    }

    @Test
    void shouldGetURLForRun() {
        Run<?, ?> run = mock(Run.class);
        ClassicDisplayURLProvider urlProvider = mock(ClassicDisplayURLProvider.class);

        when(urlProvider.getRunURL(run))
                .thenReturn("http://127.0.0.1:8080/job/github-checks-plugin/job/master/200");

        assertThat(new GitHubChecksContext(run, null, urlProvider).getURL())
                .isEqualTo("http://127.0.0.1:8080/job/github-checks-plugin/job/master/200");
    }

    private GitHubSCMFacade createGitHubSCMFacadeWithRevision(final Job<?, ?> job, final GitHubSCMSource source,
                                                              final SCMHead head, final SCMRevision revision) {
        GitHubSCMFacade facade = createGitHubSCMFacadeWithSource(job, source);

        when(facade.findHead(job)).thenReturn(Optional.ofNullable(head));
        when(facade.findRevision(source, head)).thenReturn(Optional.ofNullable(revision));

        return facade;
    }

    private GitHubSCMFacade createGitHubSCMFacadeWithCredentials(final Job<?, ?> job, final GitHubSCMSource source,
                                                                 final GitHubAppCredentials credentials,
                                                                 final String credentialsId) {
        GitHubSCMFacade facade = createGitHubSCMFacadeWithSource(job, source);

        when(source.getCredentialsId()).thenReturn(credentialsId);
        when(facade.findGitHubAppCredentials(job, credentialsId)).thenReturn(Optional.ofNullable(credentials));

        return facade;
    }

    private GitHubSCMFacade createGitHubSCMFacadeWithSource(final Job<?, ?> job, final GitHubSCMSource source) {
        GitHubSCMFacade facade = mock(GitHubSCMFacade.class);

        when(facade.findGitHubSCMSource(job)).thenReturn(Optional.ofNullable(source));

        return facade;
    }
}
