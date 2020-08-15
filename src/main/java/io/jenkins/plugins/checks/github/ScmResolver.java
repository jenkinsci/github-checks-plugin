package io.jenkins.plugins.checks.github;

import java.util.Collection;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import jenkins.triggers.SCMTriggerItem;

import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.NullSCM;
import hudson.scm.SCM;

/**
 * Resolves the used SCM in a given build.
 *
 * @author Ullrich Hafner
 */
// TODO: this class is copied from the forensics-api-plugin
public class ScmResolver {
    /**
     * Returns the SCM in a given build. If no SCM can be determined, then a {@link NullSCM} instance will be returned.
     *
     * @param run
     *         the build to get the SCM from
     *
     * @return the SCM
     */
    public SCM getScm(final Run<?, ?> run) {
        return getScm(run.getParent());
    }

    /**
     * Returns the SCM in a given job. If no SCM can be determined, then a {@link NullSCM} instance will be returned.
     *
     * @param job
     *         the job to get the SCM from
     *
     * @return the SCM
     */
    public SCM getScm(final Job<?, ?> job) {
        if (job instanceof AbstractProject) {
            return extractFromProject((AbstractProject<?, ?>) job);
        }
        else if (job instanceof SCMTriggerItem) {
            return extractFromPipeline(job);
        }
        return new NullSCM();
    }

    private SCM extractFromPipeline(final Job<?, ?> job) {
        Collection<? extends SCM> scms = ((SCMTriggerItem) job).getSCMs();
        if (!scms.isEmpty()) {
            return scms.iterator().next(); // TODO: what should we do if more than one SCM has been used
        }

        if (job instanceof WorkflowJob) {
            FlowDefinition definition = ((WorkflowJob) job).getDefinition();
            if (definition instanceof CpsScmFlowDefinition) {
                return ((CpsScmFlowDefinition) definition).getScm();
            }
        }

        return new NullSCM();
    }

    private SCM extractFromProject(final AbstractProject<?, ?> job) {
        if (job.getScm() != null) {
            return job.getScm();
        }

        SCM scm = job.getRootProject().getScm();
        if (scm != null) {
            return scm;
        }

        return new NullSCM();
    }
}
