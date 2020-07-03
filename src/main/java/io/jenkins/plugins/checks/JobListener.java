package io.jenkins.plugins.checks;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;
import io.jenkins.plugins.checks.api.ChecksStatus;

// TODO: Refactor to remove the redundant code
/**
 * A listener which publishes different statuses through the checks API based on the stage of the {@link Run}.
 */
@Extension
public class JobListener extends RunListener<Run<?, ?>> {
    private static final String CHECKS_NAME = "Jenkins";

    /**
     * {@inheritDoc}
     *
     * When a job is initializing, creates a check to keep track of the {@code run}.
     */
    @Override
    public void onInitialize(final Run run) {
        ChecksPublisher publisher = ChecksPublisherFactory.fromRun(run);
        publisher.publish(new ChecksDetailsBuilder()
                .withName(CHECKS_NAME)
                .withStatus(ChecksStatus.QUEUED)
                .build());
    }

    /**
     * {@inheritDoc}
     *
     * When a job is starting, updates the check of the {@code run} to started.
     */
    @Override
    public void onStarted(final Run run, final TaskListener listener) {
        ChecksPublisher publisher = ChecksPublisherFactory.fromRun(run);
        publisher.publish(new ChecksDetailsBuilder()
                .withName(CHECKS_NAME)
                .withStatus(ChecksStatus.IN_PROGRESS)
                .build());
    }

    /**
     * {@inheritDoc}
     *
     * When a job is completed, completes the check of the {@code run}.
     */
    @Override
    public void onCompleted(final Run run, @NonNull final TaskListener listener) {
        ChecksPublisher publisher = ChecksPublisherFactory.fromRun(run);
        // TODO: extract result from run
        publisher.publish(new ChecksDetailsBuilder()
                .withName(CHECKS_NAME)
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .build());
    }
}
