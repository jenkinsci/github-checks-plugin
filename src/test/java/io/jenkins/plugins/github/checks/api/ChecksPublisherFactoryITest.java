package io.jenkins.plugins.github.checks.api;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import io.jenkins.plugins.github.checks.ChecksPublisher;
import io.jenkins.plugins.github.checks.ChecksPublisher.NullChecksPublisher;

import static org.assertj.core.api.Assertions.*;

public class ChecksPublisherFactoryITest {
    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    public void shouldReturnNullChecksPublisherWhenUseFreestyleRun() throws Exception {
        FreeStyleProject job = rule.createFreeStyleProject();
        FreeStyleBuild run = rule.buildAndAssertSuccess(job);

        ChecksPublisher publisher = ChecksPublisherFactory.fromRun(run);
        assertThat(publisher).isInstanceOf(NullChecksPublisher.class);
    }
}
