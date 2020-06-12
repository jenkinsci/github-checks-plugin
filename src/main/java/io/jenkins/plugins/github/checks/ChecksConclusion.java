package io.jenkins.plugins.github.checks;

import org.kohsuke.github.GHCheckRun.Conclusion;

public enum ChecksConclusion {
    NONE(null),
    ACTION_REQUIRED(Conclusion.ACTION_REQUIRED),
    SKIPPED(Conclusion.CANCELLED), // TODO: need a PR for GitHub API to add that
    CANCELED(Conclusion.CANCELLED),
    TIME_OUT(Conclusion.TIMED_OUT),
    FAILURE(Conclusion.FAILURE),
    NEUTRAL(Conclusion.NEUTRAL),
    SUCCESS(Conclusion.SUCCESS);

    private final Conclusion checkRunConclusion;

    ChecksConclusion(final Conclusion checkRunConclusion) {
        this.checkRunConclusion = checkRunConclusion;
    }

    public Conclusion toCheckRunConclusion() {
        return checkRunConclusion;
    }
}
