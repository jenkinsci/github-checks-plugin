package io.jenkins.plugins.checks.github;

import java.util.Optional;

import edu.hm.hafner.util.VisibleForTesting;
import hudson.model.Job;
import hudson.model.TaskListener;
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
    protected Optional<ChecksPublisher> createPublisher(final Run<?, ?> run, final TaskListener listener) {
        return createPublisher(new GitHubChecksContext(run), listener);
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Job<?, ?> job, final TaskListener listener) {
        return createPublisher(new GitHubChecksContext(job), listener);
    }

    protected Optional<ChecksPublisher> createPublisher(final GitHubChecksContext context,
                                                        final TaskListener listener) {
        Job<?, ?> job = context.getJob();
        Optional<GitHubSCMSource> source = scmFacade.findGitHubSCMSource(job);
        if (!source.isPresent()) {
            listener.getLogger().println("Failed creating GitHub checks publisher: no GitHub SCM source found.");
            return Optional.empty();
        }

        String credentialsId = source.get().getCredentialsId();
        if (credentialsId == null
                || !scmFacade.findGitHubAppCredentials(job, credentialsId).isPresent()) {
            listener.getLogger().println("Failed creating GitHub checks publisher: no GitHub APP credentials found.");
            return Optional.empty();
        }

        listener.getLogger().println("Using GitHub checks publisher.");
        return Optional.of(new GitHubChecksPublisher(context, listener));
    }
}
