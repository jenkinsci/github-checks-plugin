package io.jenkins.plugins.checks.github.config;

import hudson.util.StreamTaskListener;
import io.jenkins.plugins.checks.github.GitHubChecksPublisherFactory;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.jvnet.hudson.test.JenkinsRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link GitHubChecksConfig}.
 */
public class GitHubChecksConfigITest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * When a job has not {@link org.jenkinsci.plugins.github_branch_source.GitHubSCMSource} or
     * {@link hudson.plugins.git.GitSCM}, the default config should be used and no verbose log should be output.
     */
    @Test
    public void shouldUseDefaultConfigWhenNoSCM() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GitHubChecksPublisherFactory.fromJob(j.createFreeStyleProject(), new StreamTaskListener(os, StandardCharsets.UTF_8));

        assertThat(os.toString()).doesNotContain("Causes for no suitable publisher found: ");
    }
}
