package io.jenkins.plugins.checks.api;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import io.jenkins.plugins.checks.api.ChecksPublisher.NullChecksPublisher;

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
