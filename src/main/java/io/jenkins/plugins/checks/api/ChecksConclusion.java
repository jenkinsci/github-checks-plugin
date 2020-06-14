package io.jenkins.plugins.checks.api;

public enum ChecksConclusion {
    NONE,
    ACTION_REQUIRED,
    SKIPPED, // TODO: need a PR for GitHub API to add that
    CANCELED,
    TIME_OUT,
    FAILURE,
    NEUTRAL,
    SUCCESS
}
