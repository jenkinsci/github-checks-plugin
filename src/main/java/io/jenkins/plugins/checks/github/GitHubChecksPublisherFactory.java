package io.jenkins.plugins.checks.github;

import java.util.Optional;

import edu.hm.hafner.util.VisibleForTesting;
import hudson.model.Job;
import hudson.model.Queue;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import hudson.Extension;
import hudson.model.Run;

import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;

/**
 * An factory which produces {@link GitHubChecksPublisher}.
 */
@Extension
public class GitHubChecksPublisherFactory extends ChecksPublisherFactory {
    private final GitHubSCMFacade scmFacade;

    /**
     * Creates a new instance of {@link GitHubChecksPublisherFactory}.
     */
    public GitHubChecksPublisherFactory() {
        this(new GitHubSCMFacade());
    }

    @VisibleForTesting
    GitHubChecksPublisherFactory(final GitHubSCMFacade scmFacade) {
        super();

        this.scmFacade = scmFacade;
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Run<?, ?> run) {
        return createPublisher(run.getParent());
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Queue.Item item) {
        if (!(item.task instanceof Job)) {
            return Optional.empty();
        }

        return createPublisher((Job<?, ?>)item.task);
    }

    protected Optional<ChecksPublisher> createPublisher(final Job<?, ?> job) {
        Optional<GitHubSCMSource> source = scmFacade.findGitHubSCMSource(job);
        if (!source.isPresent()) {
            return Optional.empty();
        }

        String credentialsId = source.get().getCredentialsId();
        if (credentialsId == null
                || !scmFacade.findGitHubAppCredentials(job, credentialsId).isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new GitHubChecksPublisher(job));
    }
}
