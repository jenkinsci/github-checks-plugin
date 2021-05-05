package io.jenkins.plugins.checks.github.config;

import hudson.util.StreamTaskListener;
import io.jenkins.plugins.checks.github.GitHubChecksPublisherFactory;
import io.jenkins.plugins.util.IntegrationTestWithJenkinsPerSuite;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link GitHubChecksConfig}.
 */
public class GitHubChecksConfigITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * When a job has not {@link org.jenkinsci.plugins.github_branch_source.GitHubSCMSource} or
     * {@link hudson.plugins.git.GitSCM}, the default config should be used and no verbose log should be output.
     */
    @Test
    public void shouldUseDefaultConfigWhenNoSCM() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GitHubChecksPublisherFactory.fromJob(createFreeStyleProject(), new StreamTaskListener(os));

        assertThat(os.toString()).doesNotContain("Causes for no suitable publisher found: ");
    }
}