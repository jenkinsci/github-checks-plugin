package io.jenkins.plugins.github.checks.api;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;
import hudson.ExtensionPoint;
import hudson.model.Run;

import io.jenkins.plugins.github.checks.ChecksContext;
import io.jenkins.plugins.github.checks.GitHubChecksClient;

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
        GitHubChecksClient.createCheckRun(new ChecksContext(run), details);
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
        GitHubChecksClient.updateCheckRun(new ChecksContext(run), details);
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
    public default void publishToComplete(final Run<?, ?> run, final ChecksDetails details) {
        GitHubChecksClient.completeCheckRun(new ChecksContext(run), details);
    }
}
