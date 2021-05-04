package io.jenkins.plugins.checks.github.config;

public class DefaultGitHubChecksConfig implements GitHubChecksConfig {
    @Override
    public boolean isVerbose() {
        return false;
    }
}
