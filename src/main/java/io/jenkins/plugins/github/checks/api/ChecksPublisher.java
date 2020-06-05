package io.jenkins.plugins.github.checks.api;

import java.util.Set;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;
import hudson.ExtensionPoint;
import hudson.model.Run;

import io.jenkins.plugins.github.checks.ChecksClient;
import io.jenkins.plugins.github.checks.ChecksStatus;

/**
 * A publisher API for consumers to publish checks.
 */
@Restricted(Beta.class)
public interface ChecksPublisher extends ExtensionPoint {
    /**
     * Returns the name of a check.
     *
     * @return the name of a check
     */
    public abstract String getName();

    /**
     * Returns the <code>ChecksStatus</code>(s) the publisher wants checks plugin to automatically set.
     *
     * @return
     *         Returns the <code>ChecksStatus</code>(s) the publisher wants checks plugin to automatically set.
     */
    public abstract Set<ChecksStatus> autoStatus();

    /**
     * Publish a new check.
     *
     * By default, this method only use the <code>details</code> without any changes to create a check.
     *
     * @param run
     *         the run which creates this check
     * @param details
     *         the checks
     */
    public default void publishToQueued(final Run<? ,?> run, final ChecksDetails details) {
        ChecksClient.createCheckRun(run, details);
    }

    /**
     * Update a check.
     *
     * By default, this method only use the <code>details</code> without any changes to update a check.
     *
     * @param run
     *         the run which creates this check
     * @param details
     *         the checks
     */
    public default void publishToInProgress(final Run<?, ?> run, final ChecksDetails details) {
        ChecksClient.updateCheckRun(run, details);
    }

    /**
     * Complete a check.
     *
     * By default, this method only use the <code>details</code> without any changes to complete a check.
     *
     * @param run
     *         the run which creates this check
     * @param details
     *         the checks
     */
    public default void publishToCompleted (final Run<?, ?> run, final ChecksDetails details) {
        ChecksClient.completeCheckRun(run, details);
    }
}
