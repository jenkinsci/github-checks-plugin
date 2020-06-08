package io.jenkins.plugins.github.checks;

public enum ChecksConclusion {
    NONE,
    ACTION_REQUIRED,
    SKIPPED,
    CANCELED,
    TIME_OUT,
    FAILURE,
    NEUTRAL,
    SUCCESS
}
