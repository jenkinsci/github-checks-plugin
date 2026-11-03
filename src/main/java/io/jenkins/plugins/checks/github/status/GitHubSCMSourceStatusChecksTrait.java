package io.jenkins.plugins.checks.github.status;

import io.jenkins.plugins.checks.github.GitHubChecksGlobalConfig;
import org.apache.commons.lang3.StringUtils;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Discovery;

import io.jenkins.plugins.checks.status.AbstractStatusChecksProperties;

/**
 * Traits to control {@link AbstractStatusChecksProperties} for jobs using
 * {@link GitHubSCMSource}.
 */
@SuppressWarnings("PMD.DataClass")
public class GitHubSCMSourceStatusChecksTrait extends SCMSourceTrait implements GitHubStatusChecksConfigurations {
    private boolean skip = false;
    private boolean skipNotifications = false;
    private boolean unstableBuildNeutral = false;
    private String name = "Jenkins";
    private boolean suppressLogs = false;
    private boolean skipProgressUpdates = DescriptorImpl.defaultSkipProgressUpdates;

    public GitHubChecksGlobalConfig globalConfig = GitHubChecksGlobalConfig.get();
    public boolean enforceSkipProgressUpdates = GitHubChecksGlobalConfig.get().isEnforceSkipProgressUpdates();

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

    @Override
    public boolean isUnstableBuildNeutral() {
        return unstableBuildNeutral;
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

    @Override
    public boolean isEnforceSkipProgressUpdates() { return enforceSkipProgressUpdates; }

    @Override
    public boolean isSuppressLogs() {
        return suppressLogs;
    }

    @Override
    public boolean isSkipProgressUpdates() {
        return skipProgressUpdates;
    }

    @DataBoundSetter
    public void setSkipProgressUpdates(final boolean skipProgressUpdates) {
        this.skipProgressUpdates = skipProgressUpdates;
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
    public void setUnstableBuildNeutral(final boolean unstableBuildNeutral) {
        this.unstableBuildNeutral = unstableBuildNeutral;
    }

    @DataBoundSetter
    public void setSkipNotifications(final boolean skipNotifications) {
        this.skipNotifications = skipNotifications;
    }

    @DataBoundSetter
    public void setSuppressLogs(final boolean suppressLogs) {
        this.suppressLogs = suppressLogs;
    }

    @Override
    protected void decorateContext(final SCMSourceContext<?, ?> context) {
        if (isSkipNotifications() && context instanceof GitHubSCMSourceContext) {
            ((GitHubSCMSourceContext)context).withNotificationsDisabled(true);
        }
    }

    /**
     * Descriptor implementation for {@link GitHubSCMSourceStatusChecksTrait}.
     */
    @Symbol("gitHubStatusChecks")
    @Extension
    @Discovery
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {
        public static final boolean defaultSkipProgressUpdates;
        public static final boolean enforceSkipProgressUpdates;

        static {
            enforceSkipProgressUpdates = GitHubChecksGlobalConfig.get().isEnforceSkipProgressUpdates();
            defaultSkipProgressUpdates = true;
        }
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
