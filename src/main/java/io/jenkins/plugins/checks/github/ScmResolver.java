package io.jenkins.plugins.checks.github;

import java.util.Collection;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import jenkins.triggers.SCMTriggerItem;

import hudson.model.AbstractBuild;
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
        Job<?, ?> job = run.getParent();
        if (run instanceof AbstractBuild) {
            return extractFromProject((AbstractBuild<?, ?>) run);
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

    private SCM extractFromProject(final AbstractBuild<?, ?> run) {
        AbstractProject<?, ?> project = run.getProject();
        if (project.getScm() != null) {
            return project.getScm();
        }

        SCM scm = project.getRootProject().getScm();
        if (scm != null) {
            return scm;
        }

        return new NullSCM();
    }
}
