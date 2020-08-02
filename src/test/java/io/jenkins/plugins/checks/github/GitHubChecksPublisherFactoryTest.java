package io.jenkins.plugins.checks.github;

import hudson.model.*;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitHubChecksPublisherFactoryTest {
    @Test
    void shouldCreateGitHubChecksPublisherFromRun() {
        Run run = mock(Run.class);
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(run.getParent()).thenReturn(job);
        when(scmFacade.findGitHubSCMSource(run.getParent())).thenReturn(Optional.of(source));
        when(source.getCredentialsId()).thenReturn("credentials id");
        when(scmFacade.findGitHubAppCredentials(run.getParent(), "credentials id"))
                .thenReturn(Optional.of(credentials));

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade);
        assertThat(factory.createPublisher(run))
                .isPresent()
                .containsInstanceOf(GitHubChecksPublisher.class);
    }

    @Test
    void shouldReturnGitHubChecksPublisherForQueueItem() {
        Project project = mock(Project.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(scmFacade.findGitHubSCMSource(project)).thenReturn(Optional.of(source));
        when(source.getCredentialsId()).thenReturn("credentials id");
        when(scmFacade.findGitHubAppCredentials(project, "credentials id"))
                .thenReturn(Optional.empty());

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade);
        assertThat(factory.createPublisher(new Queue.WaitingItem(
                Calendar.getInstance(), project, Collections.emptyList())))
                .isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenTaskOfQueueItemIsNotInstanceOfJob() {
        Queue.Task task = mock(Queue.Task.class);
        assertThat(task).isNotInstanceOf(Job.class);

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory();
        assertThat(factory.createPublisher(new Queue.WaitingItem(
                Calendar.getInstance(), task, Collections.emptyList())))
                .isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenNoGitHubSCMSourceIsConfigured() {
        Run run = mock(Run.class);

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory();
        assertThat(factory.createPublisher(run))
                .isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenNoCredentialsIsConfigured() {
        Run run = mock(Run.class);
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(run.getParent()).thenReturn(job);
        when(scmFacade.findGitHubSCMSource(run.getParent())).thenReturn(Optional.of(source));
        when(source.getCredentialsId()).thenReturn(null);

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade);
        assertThat(factory.createPublisher(run))
                .isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenNoGitHubAppCredentialsIsConfigured() {
        Run run = mock(Run.class);
        Job job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(run.getParent()).thenReturn(job);
        when(scmFacade.findGitHubSCMSource(run.getParent())).thenReturn(Optional.of(source));
        when(source.getCredentialsId()).thenReturn("credentials id");
        when(scmFacade.findGitHubAppCredentials(run.getParent(), "credentials id"))
                .thenReturn(Optional.empty());

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade);
        assertThat(factory.createPublisher(run))
                .isNotPresent();
    }
}