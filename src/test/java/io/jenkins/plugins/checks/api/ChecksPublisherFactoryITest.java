package io.jenkins.plugins.checks.api;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.checks.api.ChecksPublisher.NullChecksPublisher;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests if the {@link ChecksPublisherFactory} produces the right {@link ChecksPublisher} based on the Jenkins context.
 */
public class ChecksPublisherFactoryITest {
    /**
     * A rule which provides a Jenkins instance.
     */
    @Rule
    public JenkinsRule rule = new JenkinsRule();

    /**
     * A {@link NullChecksPublisher} should be returned when creating the {@link ChecksPublisher} with a {@link Run}
     * of {@link FreeStyleProject}.
     *
     * @throws Exception if fails to freestyle project or build
     */
    @Test
    public void shouldReturnNullChecksPublisherWhenUseFreestyleRun() throws Exception {
        FreeStyleProject job = rule.createFreeStyleProject();
        FreeStyleBuild run = rule.buildAndAssertSuccess(job);

        ChecksPublisher publisher = ChecksPublisherFactory.fromRun(run);
        assertThat(publisher).isInstanceOf(NullChecksPublisher.class);
    }
}
