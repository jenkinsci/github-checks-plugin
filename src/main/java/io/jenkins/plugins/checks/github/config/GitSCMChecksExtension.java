package io.jenkins.plugins.checks.github.config;

import hudson.Extension;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * GitHub checks configurations for freestyle jobs with {@link hudson.plugins.git.GitSCM}.
 */
@Extension
public class GitSCMChecksExtension extends GitSCMExtension implements GitHubChecksConfig {
    private boolean verboseConsoleLog;

    /**
     * Constructor for stapler.
     */
    @DataBoundConstructor
    public GitSCMChecksExtension() {
        super();
    }

    @DataBoundSetter
    public void setVerboseConsoleLog(final boolean verboseConsoleLog) {
        this.verboseConsoleLog = verboseConsoleLog;
    }

    @Override
    public boolean isVerboseConsoleLog() {
        return verboseConsoleLog;
    }

    /**
     * Descriptor for {@link GitSCMChecksExtension}.
     */
    @Extension
    public static class DescriptorImpl extends GitSCMExtensionDescriptor {
        /**
         * Returns the display name.
         *
         * @return "Configure GitHub Checks"
         */
        @Override
        public String getDisplayName() {
            return "Configure GitHub Checks";
        }
    }
}
