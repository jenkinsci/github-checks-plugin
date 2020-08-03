package io.jenkins.plugins.checks.github;

import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.util.JenkinsFacade;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GitHubChecksContextTest {
    @Test
    void shouldGetHeadShaFromMasterBranch() {
        Job job = mock(Job.class);
        SCMHead head = mock(SCMHead.class);
        AbstractGitSCMSource.SCMRevisionImpl revision = mock(AbstractGitSCMSource.SCMRevisionImpl.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(revision.getHash()).thenReturn("a1b2c3");

        assertThat(new GitHubChecksContext(job, createGitHubSCMFacadeWithRevision(job, source, head, revision))
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

        assertThat(new GitHubChecksContext(job, createGitHubSCMFacadeWithRevision(job, source, head, revision))
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

        assertThatThrownBy(
                () -> new GitHubChecksContext(job, createGitHubSCMFacadeWithRevision(job, source, head, null))
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

        assertThatThrownBy(
                () -> new GitHubChecksContext(job, createGitHubSCMFacadeWithRevision(job, source, head, revision))
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

        assertThat(new GitHubChecksContext(job, createGitHubSCMFacadeWithSource(job, source)).getRepository())
                .isEqualTo("jenkinsci/github-checks-plugin");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetRepositoryButNoGitHubSCMSourceAvailable() {
        Job job = mock(Job.class);
        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(() -> new GitHubChecksContext(job, createGitHubSCMFacadeWithSource(job, null))
                .getRepository())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No GitHub SCM source available for job: github-checks-plugin");
    }

    @Test
    void shouldGetCredentials() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);

        assertThat(new GitHubChecksContext(job, createGitHubSCMFacadeWithCredentials(job, source, credentials, "1"))
                .getCredentials())
                .isEqualTo(credentials);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetCredentialsButNoCredentialsAvailable() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(
                () -> new GitHubChecksContext(job, createGitHubSCMFacadeWithCredentials(job, source, null, null))
                    .getCredentials())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No credentials available for job: github-checks-plugin");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenGetCredentialsButNoGitHubAPPCredentialsAvailable() {
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(job.getName()).thenReturn("github-checks-plugin");

        assertThatThrownBy(
                () -> new GitHubChecksContext(job, createGitHubSCMFacadeWithCredentials(job, source, null, "1"))
                        .getCredentials())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No GitHub APP credentials available for job: github-checks-plugin");
    }

    @Test
    void shouldGetURLForQueuedItem() {
        Job job = mock(Job.class);
        Run<?, ?> build = mock(Run.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);

        when(job.getLastBuild()).thenReturn(build);
        when(build.isLogUpdated()).thenReturn(false);
        when(job.getNextBuildNumber()).thenReturn(200);
        when(build.getUrl()).thenReturn("job/github-checks-plugin/job/master/199");
        when(jenkinsFacade.getAbsoluteUrl("job", "github-checks-plugin", "job", "master", "200"))
                .thenReturn("http://127.0.0.1:8080/job/github-checks-plugin/job/master/200");

        assertThat(new GitHubChecksContext(job).getURL(jenkinsFacade))
                .isEqualTo("http://127.0.0.1:8080/job/github-checks-plugin/job/master/200");
    }

    @Test
    void shouldGetURLForRun() {
        Job job = mock(Job.class);
        Run<?, ?> build = mock(Run.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);

        when(job.getLastBuild()).thenReturn(build);
        when(build.isLogUpdated()).thenReturn(true);
        when(build.getUrl()).thenReturn("job/github-checks-plugin/job/master/200");
        when(jenkinsFacade.getAbsoluteUrl("job/github-checks-plugin/job/master/200"))
                .thenReturn("http://127.0.0.1:8080/job/github-checks-plugin/job/master/200");

        assertThat(new GitHubChecksContext(job).getURL(jenkinsFacade))
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
