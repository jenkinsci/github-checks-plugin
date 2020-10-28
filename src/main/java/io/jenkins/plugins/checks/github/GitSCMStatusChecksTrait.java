package io.jenkins.plugins.checks.github;

import hudson.Extension;
import io.jenkins.plugins.checks.status.StatusChecksProperties;
import jenkins.plugins.git.traits.GitSCMExtensionTrait;
import jenkins.plugins.git.traits.GitSCMExtensionTraitDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Traits to control {@link StatusChecksProperties} for jobs using {@link jenkins.plugins.git.GitSCMSource}.
 */
@SuppressWarnings("PMD.DataClass")
public class GitSCMStatusChecksTrait extends GitSCMExtensionTrait<GitSCMStatusChecksExtension> {
    /**
     * Constructor for stapler.
     *
     * @param extension a {@link GitSCMStatusChecksExtension} constructed with user configurations for fields.
     */
    @DataBoundConstructor
    public GitSCMStatusChecksTrait(final GitSCMStatusChecksExtension extension) {
        super(extension);
    }

    /**
     * Descriptor implementation for {@link GitSCMStatusChecksTrait}.
     */
    @Extension
    public static class DescriptorImpl extends GitSCMExtensionTraitDescriptor {
        /**
         * Returns the display name.
         *
         * @return "Status Checks Properties"
         */
        @Override
        public String getDisplayName() {
            return "Status Checks Properties";
        }
    }
}
