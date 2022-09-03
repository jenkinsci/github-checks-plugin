package io.jenkins.plugins.checks.github.config;

import hudson.Extension;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Discovery;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.Symbol;

/**
 * GitHub checks configurations for jobs with {@link GitHubSCMSource}.
 */
@Extension
public class GitHubSCMSourceChecksTrait extends SCMSourceTrait implements GitHubChecksConfig {
    private boolean verboseConsoleLog;

    /**
     * Constructor for stapler.
     */
    @DataBoundConstructor
    public GitHubSCMSourceChecksTrait() {
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
     * Descriptor implementation for {@link GitHubSCMSourceChecksTrait}.
     */
    @Symbol("gitHubSourceChecks")
    @Extension
    @Discovery
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {
        /**
         * Returns the display name.
         *
         * @return "Configure GitHub Checks"
         */
        @Override
        public String getDisplayName() {
            return "Configure GitHub Checks";
        }

        /**
         * The {@link GitHubSCMSourceChecksTrait} is only applicable to {@link GitHubSCMSourceContext}.
         *
         * @return {@link GitHubSCMSourceContext}.class
         */
        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return GitHubSCMSourceContext.class;
        }

        /**
         * The {@link GitHubSCMSourceChecksTrait} is only applicable to {@link GitHubSCMSource}.
         *
         * @return {@link GitHubSCMSource}.class
         */
        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return GitHubSCMSource.class;
        }
    }
}
