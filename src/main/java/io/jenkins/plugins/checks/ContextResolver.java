package io.jenkins.plugins.checks;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import hudson.model.Run;
import jenkins.plugins.git.AbstractGitSCMSource.SCMRevisionImpl;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

class ContextResolver {
    @CheckForNull
    public GitHubSCMSource resolveSource(Run<?, ?> run) {
        return (GitHubSCMSource) SCMSource.SourceByItem.findSource(run.getParent());
    }

    @CheckForNull
    public String resolveHeadSha(SCMSource source, Run<?, ?> run) throws IOException, InterruptedException {
        SCMHead head = resolveHead(run);
        if (head != null) {
            return resolveHeadSha(source.fetch(head, null));
        }
        return null;
    }

    @CheckForNull
    private SCMHead resolveHead(Run<?, ?> run) {
        return SCMHead.HeadByItem.findHead(run.getParent());
    }

    @CheckForNull
    private String resolveHeadSha(SCMRevision revision) throws IllegalArgumentException {
        if (revision instanceof SCMRevisionImpl) {
            return ((SCMRevisionImpl) revision).getHash();
        } else if (revision instanceof PullRequestSCMRevision) {
            return ((PullRequestSCMRevision) revision).getPullHash();
        } else {
            return null;
        }
    }
}
