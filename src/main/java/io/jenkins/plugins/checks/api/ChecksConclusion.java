package io.jenkins.plugins.checks.api;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;

/**
 * Conclusion for a specific check. One of the conclusions (except the {@link ChecksConclusion#NONE}, which is used to
 * represent "no conclusion yet") should be provided when the {@link ChecksStatus} of a check is set to
 * {@link ChecksStatus#COMPLETED}.
 */
@Restricted(Beta.class)
public enum ChecksConclusion {
    NONE, ACTION_REQUIRED, SKIPPED, CANCELED, TIME_OUT, FAILURE, NEUTRAL, SUCCESS
}
