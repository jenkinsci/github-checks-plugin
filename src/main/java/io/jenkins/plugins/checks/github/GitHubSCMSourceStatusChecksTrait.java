package io.jenkins.plugins.checks.github;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * Traits to control {@link io.jenkins.plugins.checks.status.StatusChecksProperties} for jobs using
 * {@link GitHubSCMSource}.
 */
@SuppressWarnings("PMD.DataClass")
public class GitHubSCMSourceStatusChecksTrait extends SCMSourceTrait {
    private boolean skip = false;
    private String name = "Jenkins";

    /**
     * Constructor for stapler.
     */
    @DataBoundConstructor
    public GitHubSCMSourceStatusChecksTrait() {
        super();
    }

    /**
     * Defines the status checks name which is also used as identifier for GitHub checks.
     *
     * @return the name of status checks
     */
    public String getName() {
        return name;
    }

    /**
     * Defines whether to skip publishing status checks.
     *
     * @return true to skip publishing checks
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * Set the name of the status checks.
     *
     * @param name
     *         name of the checks
     */
    @DataBoundSetter
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Set if skip publishing status checks.
     *
     * @param skip
     *         true if skip
     */
    @DataBoundSetter
    public void setSkip(final boolean skip) {
        this.skip = skip;
    }

    /**
     * Descriptor implementation for {@link GitHubSCMSourceStatusChecksTrait}.
     */
    @Extension
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {
        /**
         * Returns the display name.
         *
         * @return "Status Checks Properties"
         */
        @Override
        public String getDisplayName() {
            return "Status Checks Properties";
        }

        /**
         * The {@link GitHubSCMSourceStatusChecksTrait} is only applicable to {@link GitHubSCMSourceContext}.
         *
         * @return {@link GitHubSCMSourceContext}.class
         */
        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return GitHubSCMSourceContext.class;
        }

        /**
         * The {@link GitHubSCMSourceStatusChecksTrait} is only applicable to {@link GitHubSCMSource}.
         *
         * @return {@link GitHubSCMSource}.class
         */
        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return GitHubSCMSource.class;
        }

        /**
         * Checks if the name of status checks is valid.
         *
         * @param name
         *         name of status checks
         * @return ok if the name is not empty
         */
        public FormValidation doCheckName(@QueryParameter final String name) {
            if (StringUtils.isBlank(name)) {
                return FormValidation.error("Name should not be empty!");
            }

            return FormValidation.ok();
        }
    }
}
