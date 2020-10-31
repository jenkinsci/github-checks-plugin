package io.jenkins.plugins.checks.github;

import hudson.Extension;
import io.jenkins.plugins.checks.status.StatusChecksProperties;
import jenkins.plugins.git.traits.GitSCMExtensionTrait;
import jenkins.plugins.git.traits.GitSCMExtensionTraitDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A trait that Warps the {@link GitSCMStatusChecksExtension} to provide {@link StatusChecksProperties}
 * for {@link jenkins.plugins.git.GitSCMSource} job.
 */
@SuppressWarnings("PMD.DataClass")
public class GitSCMSourceStatusChecksTrait extends GitSCMExtensionTrait<GitSCMStatusChecksExtension>
        implements StatusChecksConfigurations {
    private GitSCMStatusChecksExtension extension;

    /**
     * Constructor for stapler.
     *
     * @param extension a {@link GitSCMStatusChecksExtension} constructed with user configurations for fields.
     */
    @DataBoundConstructor
    public GitSCMSourceStatusChecksTrait(final GitSCMStatusChecksExtension extension) {
        super(extension);

        this.extension = extension;
    }

    @Override
    public String getName() {
        return extension.getName();
    }

    @Override
    public boolean isSkip() {
        return false;
    }
    /**
     * Descriptor implementation for {@link GitSCMSourceStatusChecksTrait}.
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
