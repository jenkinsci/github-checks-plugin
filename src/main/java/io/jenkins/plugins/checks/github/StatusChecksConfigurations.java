package io.jenkins.plugins.checks.github;

/**
 * Configurations for users to customize status checks.
 */
public interface StatusChecksConfigurations {
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
}
