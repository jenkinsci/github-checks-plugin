package io.jenkins.plugins.checks;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import hudson.model.Run;
import jenkins.plugins.git.AbstractGitSCMSource.SCMRevisionImpl;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

public class ContextResolver {
    @NonNull
    public SCMSource resolveSource(final Run<?, ?> run) {
        SCMSource source = SCMSource.SourceByItem.findSource(run.getParent());
        if (source != null) {
            return source;
        } else {
            throw new IllegalStateException("Could not resolve scm source from run: " + run);
        }
    }

    @NonNull
    public String resolveHeadSha(final SCMSource source, final Run<?, ?> run) {
        SCMHead head = resolveHead(run);
        try {
            return resolveHeadSha(source.fetch(head, null));
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(String.format("Could not resolve head sha, source: %s, run: %s",
                    source, run));
        }
    }

    @NonNull
    private SCMHead resolveHead(final Run<?, ?> run) {
        SCMHead head = SCMHead.HeadByItem.findHead(run.getParent());
        if (head == null) {
            throw new IllegalStateException("Could not resolve head from run: " + run);
        }
        return head;
    }

    @NonNull
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
