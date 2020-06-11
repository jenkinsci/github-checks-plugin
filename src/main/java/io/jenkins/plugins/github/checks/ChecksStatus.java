package io.jenkins.plugins.github.checks;

import org.kohsuke.github.GHCheckRun.Status;

public enum ChecksStatus {
    QUEUED(Status.QUEUED),
    IN_PROGRESS(Status.IN_PROGRESS),
    COMPLETED(Status.COMPLETED);

    private final Status checkRunStatus;

    ChecksStatus(final Status status) {
        this.checkRunStatus = status;
    }

    public Status toCheckRunStatus() {
        return checkRunStatus;
    }
}
