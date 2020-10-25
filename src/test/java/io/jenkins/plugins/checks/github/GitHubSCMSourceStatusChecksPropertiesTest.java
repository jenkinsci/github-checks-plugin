package io.jenkins.plugins.checks.github;

import hudson.model.Job;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitHubSCMSourceStatusChecksPropertiesTest {
    @Test
    void shouldReturnPropertiesAsTraitConfigured() {
        Job job = mock(Job.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubSCMSourceStatusChecksTrait trait = new GitHubSCMSourceStatusChecksTrait();

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(java.util.Optional.ofNullable(source));
        when(source.getTraits()).thenReturn(Collections.singletonList(trait));

        trait.setName("status checks");
        trait.setSkip(false);

        GitHubSCMSourceStatusChecksProperties properties = new GitHubSCMSourceStatusChecksProperties(scmFacade);
        assertThat(properties.isApplicable(job)).isTrue();
        assertThat(properties.getName(job)).isEqualTo("status checks");
        assertThat(properties.isSkip(job)).isFalse();
    }

    @Test
    void shouldReturnDefaultPropertiesWhenNoTraitAdded() {
        Job job = mock(Job.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(java.util.Optional.ofNullable(source));

        GitHubSCMSourceStatusChecksProperties properties = new GitHubSCMSourceStatusChecksProperties(scmFacade);
        assertThat(properties.isApplicable(job)).isTrue();
        assertThat(properties.getName(job)).isEqualTo("Jenkins");
        assertThat(properties.isSkip(job)).isFalse();
    }

    @Test
    void shouldNotApplicableToJobWithoutGitHubSCMSource() {
        Job job = mock(Job.class);
        assertThat(new GitHubSCMSourceStatusChecksProperties().isApplicable(job)).isFalse();
    }

    @Test
    void shouldValidStatusChecksNameInDescriptor() {
        GitHubSCMSourceStatusChecksTrait.DescriptorImpl descriptor = new GitHubSCMSourceStatusChecksTrait.DescriptorImpl();

        assertThat(descriptor.doCheckName("Name Should Not Be Blank"))
                .hasFieldOrPropertyWithValue("kind", FormValidation.Kind.OK);

        assertThat(descriptor.doCheckName("  "))
                .hasFieldOrPropertyWithValue("kind", FormValidation.Kind.ERROR)
                .hasMessage("Name should not be empty!");
    }
}
