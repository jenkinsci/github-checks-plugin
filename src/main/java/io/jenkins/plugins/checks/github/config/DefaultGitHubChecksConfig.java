package io.jenkins.plugins.checks.github.config;

/**
 * Default implementation for {@link GitHubChecksConfig}.
 */
public class DefaultGitHubChecksConfig implements GitHubChecksConfig {
    @Override
    public boolean isVerboseConsoleLog() {
        return false;
    }
}
