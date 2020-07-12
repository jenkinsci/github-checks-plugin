package io.jenkins.plugins.checks.github;

import hudson.model.Run;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitHubChecksPublisherFactoryTest {
    @Test
    void shouldCreateGitHubChecksPublisher() {
        Run run = mock(Run.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(scmFacade.findGitHubSCMSource(run)).thenReturn(Optional.of(source));
        when(source.getCredentialsId()).thenReturn("credentials id");
        when(scmFacade.findGitHubAppCredentials(run, "credentials id")).thenReturn(Optional.of(credentials));

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade);
        assertThat(factory.createPublisher(run))
                .isPresent()
                .containsInstanceOf(GitHubChecksPublisher.class);
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
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(scmFacade.findGitHubSCMSource(run)).thenReturn(Optional.of(source));
        when(source.getCredentialsId()).thenReturn(null);

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade);
        assertThat(factory.createPublisher(run))
                .isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenNoGitHubAppCredentialsIsConfigured() {
        Run run = mock(Run.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(scmFacade.findGitHubSCMSource(run)).thenReturn(Optional.of(source));
        when(source.getCredentialsId()).thenReturn("credentials id");
        when(scmFacade.findGitHubAppCredentials(run, "credentials id")).thenReturn(Optional.empty());

        GitHubChecksPublisherFactory factory = new GitHubChecksPublisherFactory(scmFacade);
        assertThat(factory.createPublisher(run))
                .isNotPresent();
    }
}