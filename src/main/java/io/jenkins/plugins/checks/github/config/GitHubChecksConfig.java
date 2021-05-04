package io.jenkins.plugins.checks.github.config;

/**
 * Project-level configurations for users to customize GitHub checks.
 */
public interface GitHubChecksConfig {
    /**
     * Defines whether to output verbose console log
     *
     * @return true for verbose log
     */
    boolean isVerboseConsoleLog();
}
