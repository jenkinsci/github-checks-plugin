package io.jenkins.plugins.checks.github;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Job;
import hudson.security.ACL;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class GitHubSCMFacade {
    Optional<GitHubSCMSource> findGitHubSCMSource(final Job<?, ?> job) {
        SCMSource source = SCMSource.SourceByItem.findSource(job);
        return source instanceof GitHubSCMSource ? Optional.of((GitHubSCMSource) source) : Optional.empty();
    }

    Optional<GitHubAppCredentials> findGitHubAppCredentials(final Job<?, ?> job, final String credentialsId) {
        List<GitHubAppCredentials> credentials = CredentialsProvider.lookupCredentials(
                GitHubAppCredentials.class, job, ACL.SYSTEM, Collections.emptyList());
        GitHubAppCredentials appCredentials =
                CredentialsMatchers.firstOrNull(credentials, CredentialsMatchers.withId(credentialsId));
        return Optional.ofNullable(appCredentials);
    }

    Optional<SCMHead> findHead(final Job<?, ?> job) {
        SCMHead head = SCMHead.HeadByItem.findHead(job);
        return Optional.ofNullable(head);
    }

    Optional<SCMRevision> findRevision(final GitHubSCMSource source, final SCMHead head) {
        try {
            return Optional.ofNullable(source.fetch(head, null));
        }
        catch (IOException | InterruptedException e) {
            throw new IllegalStateException(String.format("Could not fetch revision from repository: %s and branch: %s",
                    source.getRepoOwner() + "/" + source.getRepository(), head.getName()), e);
        }
    }
}
