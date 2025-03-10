package io.jenkins.plugins.checks.github.config;

import hudson.util.StreamTaskListener;
import io.jenkins.plugins.checks.github.GitHubChecksPublisherFactory;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link GitHubChecksConfig}.
 */
@WithJenkins
class GitHubChecksConfigITest {

    /**
     * When a job has not {@link org.jenkinsci.plugins.github_branch_source.GitHubSCMSource} or
     * {@link hudson.plugins.git.GitSCM}, the default config should be used and no verbose log should be output.
     */
    @Test
    void shouldUseDefaultConfigWhenNoSCM(JenkinsRule j) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GitHubChecksPublisherFactory.fromJob(j.createFreeStyleProject(), new StreamTaskListener(os, StandardCharsets.UTF_8));

        assertThat(os.toString()).doesNotContain("Causes for no suitable publisher found: ");
    }
}
