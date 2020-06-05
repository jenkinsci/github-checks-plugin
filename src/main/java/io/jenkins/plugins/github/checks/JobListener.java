package io.jenkins.plugins.github.checks;

import java.util.logging.Logger;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.github.checks.api.ChecksDetails;
import io.jenkins.plugins.github.checks.api.ChecksDetails.ChecksDetailBuilder;
import io.jenkins.plugins.github.checks.api.ChecksPublisher;
import io.jenkins.plugins.util.JenkinsFacade;

@Extension
public class JobListener extends RunListener<Run<?, ?>> {
    private final JenkinsFacade jenkins;

    private static final Logger LOGGER = Logger.getLogger(JobListener.class.getName());

    public JobListener() {
        this(new JenkinsFacade());
    }

    @VisibleForTesting
    JobListener(JenkinsFacade jenkins) {
        super();

        this.jenkins = jenkins;
    }

    /**
     * {@inheritDoc}
     *
     * When a job is initializing, we create check runs implemented by consumers and set to 'pending' state.
     */
    @Override
    public void onInitialize(Run run) {
        for (ChecksPublisher publisher : jenkins.getExtensionsFor(ChecksPublisher.class)) {
            if (publisher.autoStatus().contains(ChecksStatus.Queued)) {
                ChecksDetails checks = new ChecksDetailBuilder(publisher.getName()).build();
                publisher.publishToQueued(run, checks);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * When a job is starting, we simply set all the related check runs into 'in_progress' state.
     */
    @Override
    public void onStarted(Run run, TaskListener listener) {
        for (ChecksPublisher publisher : jenkins.getExtensionsFor(ChecksPublisher.class)) {
            if (publisher.autoStatus().contains(ChecksStatus.InProgress)) {
                ChecksDetails checks = new ChecksDetailBuilder(publisher.getName()).build();
                publisher.publishToInProgress(run, checks);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * When a job is completed, we complete all the related check runs with parameters.
     */
    @Override
    public void onCompleted(Run run, @NonNull TaskListener listener) {
        for (ChecksPublisher publisher : jenkins.getExtensionsFor(ChecksPublisher.class)) {
            if (publisher.autoStatus().contains(ChecksStatus.Completed)) {
                ChecksDetails checks = new ChecksDetailBuilder(publisher.getName()).build();
                publisher.publishToCompleted(run, checks);
            }
        }
    }
}
