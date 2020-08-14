package io.jenkins.plugins.checks.github;

import java.util.Optional;

import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;

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
        return createPublisher(new GitHubSCMSourceChecksContext(run), listener);
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Job<?, ?> job, final TaskListener listener) {
        return createPublisher(new GitHubSCMSourceChecksContext(job), listener);
    }

    protected Optional<ChecksPublisher> createPublisher(final GitHubChecksContext context,
                                                        @Nullable final TaskListener listener) {
        Job<?, ?> job = context.getJob();
        Optional<GitHubSCMSource> source = scmFacade.findGitHubSCMSource(job);
        Optional<GitSCM> scm = scmFacade.findGitSCM(context.getRun());
        if (!source.isPresent() && !scm.isPresent()) {
            if (listener != null) {
                listener.getLogger().println("Skipped publishing GitHub checks: no GitHub SCM found.");
            }
            return Optional.empty();
        }

        String credentialsId;
        if (source.isPresent()) {
            credentialsId = source.get().getCredentialsId();
        }
        else {
            credentialsId = scmFacade.getUserRemoteConfig(scm.get()).getCredentialsId();
        }
        if (credentialsId == null
                || !scmFacade.findGitHubAppCredentials(job, credentialsId).isPresent()) {
            if (listener != null) {
                listener.getLogger().println("Skipped publishing GitHub checks: no GitHub APP credentials found, "
                        + "see "
                        + "https://github.com/jenkinsci/github-branch-source-plugin/blob/master/docs/github-app.adoc");
            }
            return Optional.empty();
        }

        if (listener != null) {
            listener.getLogger().println("Publishing GitHub check...");
        }
        return Optional.of(new GitHubChecksPublisher(context, listener));
    }
}
