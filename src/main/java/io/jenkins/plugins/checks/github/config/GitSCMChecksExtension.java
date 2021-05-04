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
public class GitSCMChecksExtension extends GitSCMExtension implements GitHubChecksConfig{
    private boolean verbose;

    /**
     * Constructor for stapler.
     */
    @DataBoundConstructor
    public GitSCMChecksExtension() {
        super();
    }

    @DataBoundSetter
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

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
