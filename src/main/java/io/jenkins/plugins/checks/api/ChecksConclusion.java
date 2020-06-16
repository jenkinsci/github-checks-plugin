package io.jenkins.plugins.checks.api;

/**
 * Conclusion for a specific check. One of the conclusions (except the {@link ChecksConclusion#NONE}, which is used to
 * represent "no conclusion yet") should be provided when the {@link ChecksStatus} of a check is set to
 * {@link ChecksStatus#COMPLETED}.
 */
public enum ChecksConclusion {
    NONE, ACTION_REQUIRED, SKIPPED, CANCELED, TIME_OUT, FAILURE, NEUTRAL, SUCCESS
}
