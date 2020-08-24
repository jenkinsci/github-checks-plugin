package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.util.Optional;

import hudson.EnvVars;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

class GitHubChecksPublisherFactoryTest {
    @Test
    void shouldCreateGitHubChecksPublisherFromRunForProjectWithValidGitHubSCMSource() {
        Run run = mock(Run.class);
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        SCMFacade scmFacade = mock(SCMFacade.class);

        when(run.getParent()).thenReturn(job);
        when(job.getLastBuild()).thenReturn(run);
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(source.getCredentialsId()).thenReturn("credentials id");
        when(scmFacade.findGitHubAppCredentials(job, "credentials id")).thenReturn(Optional.of(credentials));
        when(scmFacade.findRevision(source, run)).thenReturn(Optional.of(revision));
        when(scmFacade.findHash(revision)).thenReturn(Optional.of("a1b2c3"));

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade, createDisplayURLProvider(run,
                job));
        assertThat(factory.createPublisher(run, TaskListener.NULL)).containsInstanceOf(GitHubChecksPublisher.class);
    }

    @Test
    void shouldReturnGitHubChecksPublisherFromJobProjectWithValidGitHubSCMSource() {
        Run run = mock(Run.class);
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        SCMHead head = mock(SCMHead.class);
        SCMFacade scmFacade = mock(SCMFacade.class);

        when(run.getParent()).thenReturn(job);
        when(job.getLastBuild()).thenReturn(run);
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(source.getCredentialsId()).thenReturn("credentials id");
        when(scmFacade.findGitHubAppCredentials(job, "credentials id")).thenReturn(Optional.of(credentials));
        when(scmFacade.findHead(job)).thenReturn(Optional.of(head));
        when(scmFacade.findRevision(source, head)).thenReturn(Optional.of(revision));
        when(scmFacade.findHash(revision)).thenReturn(Optional.of("a1b2c3"));

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade, createDisplayURLProvider(run,
                job));
        assertThat(factory.createPublisher(job, TaskListener.NULL)).containsInstanceOf(GitHubChecksPublisher.class);
    }

    @Test
    void shouldCreateGitHubChecksPublisherFromRunForProjectWithValidGitSCM() throws IOException, InterruptedException {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        GitSCM gitSCM = mock(GitSCM.class);
        UserRemoteConfig config = mock(UserRemoteConfig.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        EnvVars envVars = mock(EnvVars.class);

        when(run.getParent()).thenReturn(job);
        when(run.getEnvironment(TaskListener.NULL)).thenReturn(envVars);
        when(envVars.get("GIT_COMMIT")).thenReturn("a1b2c3");
        when(scmFacade.getScm(job)).thenReturn(gitSCM);
        when(scmFacade.findGitSCM(run)).thenReturn(Optional.of(gitSCM));
        when(scmFacade.getUserRemoteConfig(gitSCM)).thenReturn(config);
        when(config.getCredentialsId()).thenReturn("1");
        when(scmFacade.findGitHubAppCredentials(job, "1")).thenReturn(Optional.of(credentials));
        when(config.getUrl()).thenReturn("https://github.com/jenkinsci/github-checks-plugin");

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade, createDisplayURLProvider(run,
                job));
        assertThat(factory.createPublisher(run, TaskListener.NULL)).containsInstanceOf(GitHubChecksPublisher.class);
    }

    @Test
    void shouldReturnEmptyFromRunForInvalidProject() {
        Run run = mock(Run.class);
        SCMFacade facade = mock(SCMFacade.class);
        DisplayURLProvider urlProvider = mock(DisplayURLProvider.class);

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(facade, urlProvider);
        assertThat(factory.createPublisher(run, TaskListener.NULL)).isNotPresent();
    }

    @Test
    void shouldCreateNullPublisherFromJobForInvalidProject() {
        Job job = mock(Job.class);
        SCMFacade facade = mock(SCMFacade.class);
        DisplayURLProvider urlProvider = mock(DisplayURLProvider.class);

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(facade, urlProvider);
        assertThat(factory.createPublisher(job, TaskListener.NULL))
                .isNotPresent();
    }

    private DisplayURLProvider createDisplayURLProvider(final Run<?, ?> run, final Job<?, ?> job) {
        DisplayURLProvider urlProvider = mock(DisplayURLProvider.class);

        when(urlProvider.getRunURL(run)).thenReturn(null);
        when(urlProvider.getJobURL(job)).thenReturn(null);

        return urlProvider;
    }
}
