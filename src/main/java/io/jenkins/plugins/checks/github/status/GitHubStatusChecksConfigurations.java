package io.jenkins.plugins.checks.github.status;

/**
 * Configurations for users to customize status checks.
 */
public interface GitHubStatusChecksConfigurations {
    /**
     * Defines the status checks name which is also used as identifier for GitHub checks.
     *
     * @return the name of status checks
     */
    String getName();

    /**
     * Defines whether to skip publishing status checks.
     *
     * @return true to skip publishing checks
     */
    boolean isSkip();

    /**
     * Defines whether to publish unstable builds as neutral status checks.
     *
     * @return true to publish unstable builds as neutral status checks.
     */
    boolean isUnstableBuildNeutral();

    /**
     * Defines whether to suppress log output in status checks.
     *
     * @return true to suppress logs
     */
    boolean isSuppressLogs();

    /**
     * Returns whether to suppress progress updates from the {@code io.jenkins.plugins.checks.status.FlowExecutionAnalyzer}.
     * Queued, Checkout and Completed will still run but not 'onNewHead'
     *
     * @return true if progress updates should be skipped.
     */
    boolean isSkipProgressUpdates();
}

class DefaultGitHubStatusChecksConfigurations implements GitHubStatusChecksConfigurations {
    @Override
    public String getName() {
        return "Jenkins";
    }

    @Override
    public boolean isSkip() {
        return false;
    }

    @Override
    public boolean isUnstableBuildNeutral() {
        return false;
    }

    @Override
    public boolean isSuppressLogs() {
        return false;
    }

    @Override
    public boolean isSkipProgressUpdates() {
        return false;
    }
}

