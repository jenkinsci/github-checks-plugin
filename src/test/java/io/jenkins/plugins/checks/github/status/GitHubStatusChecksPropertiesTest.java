package io.jenkins.plugins.checks.github.status;

import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.util.DescribableList;
import io.jenkins.plugins.checks.github.SCMFacade;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitHubStatusChecksPropertiesTest {

    @Test
    void shouldUsePropertiesFromGitHubSCMSourceTrait() {
        Job job = mock(Job.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubSCMSourceStatusChecksTrait trait = new GitHubSCMSourceStatusChecksTrait();

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(source.getTraits()).thenReturn(Collections.singletonList(trait));

        trait.setName("GitHub SCM Source");
        trait.setSkip(true);
        trait.setUnstableBuildNeutral(true);
        trait.setSuppressLogs(true);
        trait.setRerunActionRole("test-role");
        trait.setDisableRerunAction(true);

        assertJobWithStatusChecksProperties(job, new GitHubStatusChecksProperties(scmFacade),
                true, "GitHub SCM Source", true, true, true, "test-role", true);
    }

    @Test
    void shouldUsePropertiesFromGitSCMExtension() {
        Job job = mock(Job.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitSCM scm = mock(GitSCM.class);
        GitSCMStatusChecksExtension extension = new GitSCMStatusChecksExtension();
        DescribableList extensionList = new DescribableList(null, Collections.singletonList(extension));

        when(scmFacade.findGitSCM(job)).thenReturn(Optional.of(scm));
        when(scm.getExtensions()).thenReturn(extensionList);

        extension.setName("Git SCM");
        extension.setSkip(true);
        extension.setUnstableBuildNeutral(true);
        extension.setSuppressLogs(true);
        extension.setRerunActionRole("test-role");
        extension.setDisableRerunAction(true);

        assertJobWithStatusChecksProperties(job, new GitHubStatusChecksProperties(scmFacade),
                true, "Git SCM", true, true, true, "test-role", true);
    }

    @Test
    void shouldUseDefaultPropertiesWhenGitHubSCMSourceStatusChecksTraitIsNotAdded() {
        Job job = mock(Job.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));

        assertJobWithStatusChecksProperties(job, new GitHubStatusChecksProperties(scmFacade),
                true, "Jenkins", false, false, false, "", false);
    }

    @Test
    void shouldUseDefaultPropertiesWhenGitSCMStatusChecksExtensionIsNotAdded() {
        Job job = mock(Job.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitSCM scm = mock(GitSCM.class);
        DescribableList extensionList = new DescribableList(null, Collections.emptyList());

        when(scmFacade.findGitSCM(job)).thenReturn(Optional.of(scm));
        when(scm.getExtensions()).thenReturn(extensionList);

        assertJobWithStatusChecksProperties(job, new GitHubStatusChecksProperties(scmFacade),
                true, "Jenkins", false, false, false, "", false);
    }

    @Test
    void shouldNotApplicableToJobWithoutSupportedSCM() {
        Job job = mock(Job.class);
        SCMFacade scmFacade = mock(SCMFacade.class);

        when(scmFacade.findGitSCM(job)).thenReturn(Optional.empty());
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.empty());
        assertJobWithStatusChecksProperties(job, new GitHubStatusChecksProperties(scmFacade),
                false, "Jenkins", false, false, false, "", false);
    }

    private static void assertJobWithStatusChecksProperties(final Job job, final GitHubStatusChecksProperties properties,
                                                            final boolean isApplicable, final String name,
                                                            final boolean isSkip, final boolean isUnstableBuildNeutral,
                                                            final boolean isSuppressLogs, final String rerunActionRole,
                                                            final boolean isDisableRerunAction) {
        assertThat(properties.isApplicable(job)).isEqualTo(isApplicable);
        assertThat(properties.getName(job)).isEqualTo(name);
        assertThat(properties.isSkipped(job)).isEqualTo(isSkip);
        assertThat(properties.isUnstableBuildNeutral(job)).isEqualTo(isUnstableBuildNeutral);
        assertThat(properties.isSuppressLogs(job)).isEqualTo(isSuppressLogs);
        assertThat(properties.getRerunActionRole(job)).isEqualTo(rerunActionRole);
        assertThat(properties.isDisableRerunAction(job)).isEqualTo(isDisableRerunAction);
    }
}

