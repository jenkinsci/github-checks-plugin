package io.jenkins.plugins.checks.api;

import org.junit.jupiter.api.Test;

import hudson.model.Run;

import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksPublisher.NullChecksPublisher;

import static org.mockito.Mockito.*;

class NullChecksPublisherTest {
    @Test
    void shouldPublishNothingWhenInvokingPublish() {
        Run<?, ?> run = mock(Run.class);

        NullChecksPublisher publisher = new NullChecksPublisher(run);
        publisher.publish(new ChecksDetailsBuilder().build());
    }
}