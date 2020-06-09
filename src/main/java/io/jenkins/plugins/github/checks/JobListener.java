package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.io.UncheckedIOException;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.github.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.github.checks.api.ChecksPublisher;

@Extension
public class JobListener extends RunListener<Run<?, ?>> {
    private static final String CHECKS_NAME = "Jenkins";

    /**
     * {@inheritDoc}
     *
     * When a job is initializing, creates a check to keep track of the {@code run}.
     */
    @Override
    public void onInitialize(Run run) {
        ChecksPublisher publisher = ChecksPublisher.fromRun(run);
        try {
            publisher.publish(new ChecksDetailsBuilder(CHECKS_NAME, ChecksStatus.QUEUED).build());
        } catch (IOException e) {
            throw new UncheckedIOException("could not publish a new check for the build", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * When a job is starting, updates the check of the {@code run} to started.
     */
    @Override
    public void onStarted(Run run, TaskListener listener) {
        ChecksPublisher publisher = ChecksPublisher.fromRun(run);
        try {
            publisher.publish(new ChecksDetailsBuilder(CHECKS_NAME, ChecksStatus.IN_PROGRESS).build());
        } catch (IOException e) {
            throw new UncheckedIOException("could not start the check for the build", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * When a job is completed, completes the check of the {@code run}.
     */
    @Override
    public void onCompleted(Run run, @NonNull TaskListener listener) {
        ChecksPublisher publisher = ChecksPublisher.fromRun(run);
        try {
            // TODO: extract result from run
            publisher.publish(new ChecksDetailsBuilder(CHECKS_NAME, ChecksStatus.COMPLETED)
                    .withConclusion(ChecksConclusion.SUCCESS)
                    .build());
        } catch (IOException e) {
            throw new UncheckedIOException("could not complete the check for the build", e);
        }
    }
}
