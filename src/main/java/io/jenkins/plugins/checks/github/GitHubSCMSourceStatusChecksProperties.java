package io.jenkins.plugins.checks.github;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Job;
import hudson.util.FormValidation;
import io.jenkins.plugins.checks.status.StatusChecksProperties;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMHeadPrefilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Discovery;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.regex.Pattern;

/**
 * Status checks properties for job that use {@link GitHubSCMSource}.
 */
public class GitHubSCMSourceStatusChecksProperties extends SCMSourceTrait implements StatusChecksProperties {
    private final boolean skip;
    private final String name;

    /**
     * Constructor for stapler.
     *
     * @param skip
     *         true if skip publishing status checks
     * @param name
     *         name of the status checks
     */
    @DataBoundConstructor
    public GitHubSCMSourceStatusChecksProperties(final boolean skip, final String name) {
        this.skip = skip;
        this.name = name;
    }

    /**
     * This implementation of {@link StatusChecksProperties} is applicable only for jobs with {@link GitHubSCMSource}.
     *
     * @return true if the job is using {@link GitHubSCMSource}
     */
    @Override
    public boolean isApplicable(Job<?, ?> job) {
        return SCMSource.SourceByItem.findSource(job) instanceof GitHubSCMSource;
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
    public boolean isSkipped() {
        return skip;
    }

    /**
     * Returns the descriptor implementation of this class.
     *
     * @return the descriptor
     */
    @Override
    public SCMSourceTraitDescriptor getDescriptor() {
        return new DescriptorImpl();
    }

    /**
     * Descriptor implementation for {@link GitHubSCMSourceStatusChecksProperties}.
     */
    @Extension
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Status Checks Properties";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return GitHubSCMSourceContext.class;
        }

        /**
         * {@inheritDoc}
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
