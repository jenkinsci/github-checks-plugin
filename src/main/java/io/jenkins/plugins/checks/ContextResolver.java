package io.jenkins.plugins.checks;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import hudson.model.Run;
import jenkins.plugins.git.AbstractGitSCMSource.SCMRevisionImpl;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

public class ContextResolver {
    @CheckForNull
    public SCMSource resolveSource(final Run<?, ?> run) {
        return SCMSource.SourceByItem.findSource(run.getParent());
    }

    public String resolveHeadSha(final SCMSource source, final Run<?, ?> run) {
        SCMHead head = resolveHead(run);
        try {
            return resolveHeadSha(source.fetch(head, null));
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Could not resolve head sha, source: " + e);
        }
    }

    private SCMHead resolveHead(final Run<?, ?> run) {
        SCMHead head = SCMHead.HeadByItem.findHead(run.getParent());
        if (head == null) {
            throw new IllegalStateException("Could not resolve head from run: " + run);
        }
        return head;
    }

    private String resolveHeadSha(final SCMRevision revision) {
        if (revision instanceof SCMRevisionImpl) {
            return ((SCMRevisionImpl) revision).getHash();
        } else if (revision instanceof PullRequestSCMRevision) {
            return ((PullRequestSCMRevision) revision).getPullHash();
        } else {
            throw new IllegalStateException("Could not resolve head sha from revision type: "
                    + revision.getClass().getName());
        }
    }
}
