package io.jenkins.plugins.github.checks.api;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;
import hudson.ExtensionPoint;
import hudson.model.Run;

/**
 * This listener provides extension points to notify consumers provide parameters for checks.
 */
@Restricted(Beta.class)
public interface ChecksListener extends ExtensionPoint {
    abstract String getName();
    abstract void onQueued(final Run<?, ?> run, final ChecksBuilder checks, final AnnotationsBuilder annotations);
    abstract void onInProgress(final Run<?, ?> run, final ChecksBuilder checks, final AnnotationsBuilder annotations);
    abstract void onComplete(final Run<?, ?> run, final ChecksBuilder checks, final AnnotationsBuilder annotations);
    // abstract void onNewHead(final node stage); thoughts for pipeline
}
