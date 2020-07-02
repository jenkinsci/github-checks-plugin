package io.jenkins.plugins.checks.api;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;

/**
 * Status for a specific check.
 */
@Restricted(Beta.class)
public enum ChecksStatus {
    NONE, QUEUED, IN_PROGRESS, COMPLETED
}
