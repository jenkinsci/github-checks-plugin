package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.util.logging.Logger;

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
    private static final Logger LOGGER = Logger.getLogger(JobListener.class.getName());

    /**
     * {@inheritDoc}
     *
     * When a job is initializing, we create check runs implemented by consumers and set to 'pending' state.
     */
    @Override
    public void onInitialize(Run run) {
        ChecksPublisher publisher = ChecksPublisher.fromRun(run);
        try {
            publisher.publish(new ChecksDetailsBuilder(CHECKS_NAME, ChecksStatus.QUEUED).build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     *
     * When a job is starting, we simply set all the related check runs into 'in_progress' state.
     */
    @Override
    public void onStarted(Run run, TaskListener listener) {
        ChecksPublisher publisher = ChecksPublisher.fromRun(run);
        try {
            publisher.publish(new ChecksDetailsBuilder(CHECKS_NAME, ChecksStatus.IN_PROGRESS).build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     *
     * When a job is completed, we complete all the related check runs with parameters.
     */
    @Override
    public void onCompleted(Run run, @NonNull TaskListener listener) {
        ChecksPublisher publisher = ChecksPublisher.fromRun(run);
        try {
            publisher.publish(new ChecksDetailsBuilder(CHECKS_NAME, ChecksStatus.COMPLETED)
                    .withConclusion(ChecksConclusion.SUCCESS)
                    .build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
