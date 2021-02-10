package io.jenkins.plugins.checks.github.status;

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
public class GitHubSCMSourceStatusChecksTrait extends SCMSourceTrait implements GitHubStatusChecksConfigurations {
    private boolean skip = false;
    private boolean skipNotifications = false;
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
    @Override
    public String getName() {
        return name;
    }

    /**
     * Defines whether to skip publishing status checks.
     *
     * @return true to skip publishing checks
     */
    @Override
    public boolean isSkip() {
        return skip;
    }

    /**
     * Defines whether to skip notifications from {@link org.jenkinsci.plugins.github_branch_source.GitHubBuildStatusNotification}
     * which utilizes the <a href="https://docs.github.com/en/free-pro-team@latest/rest/reference/repos#statuses">GitHub Status API</a>.
     *
     * @return true to skip notifications
     */
    public boolean isSkipNotifications() {
        return skipNotifications;
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

    @DataBoundSetter
    public void setSkipNotifications(final boolean skipNotifications) {
        this.skipNotifications = skipNotifications;
    }

    @Override
    protected void decorateContext(final SCMSourceContext<?, ?> context) {
        if (isSkipNotifications()) {
            ((GitHubSCMSourceContext)context).withNotificationsDisabled(true);
        }
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
