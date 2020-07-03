package io.jenkins.plugins.checks.api;

import java.util.List;
import java.util.Optional;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import hudson.ExtensionPoint;
import hudson.model.Run;

import io.jenkins.plugins.checks.api.ChecksPublisher.NullChecksPublisher;
import io.jenkins.plugins.checks.github.GitHubChecksPublisher;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * A publisher API for consumers to publish checks.
 */
@Restricted(Beta.class)
public abstract class ChecksPublisherFactory implements ExtensionPoint {
    /**
     * Creates proper {@link ChecksPublisher} for the given context, e.g. a {@link GitHubChecksPublisher} for a context
     * with {@link GitHubSCMSource}.
     *
     * @param run
     *         a Jenkins run
     * @return the created {@link ChecksPublisher}
     */
    protected abstract Optional<ChecksPublisher> createPublisher(Run<?, ?> run);

    /**
     * Returns a suitable publisher for the run.
     *
     * @param run
     *         a Jenkins build
     * @return a publisher suitable for the run
     */
    public static ChecksPublisher fromRun(final Run<?, ?> run) {
        return findAllPublisherFactories().stream()
                .map(factory -> factory.createPublisher(run))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(new NullChecksPublisher());
    }

    private static List<ChecksPublisherFactory> findAllPublisherFactories() {
        return new JenkinsFacade().getExtensionsFor(ChecksPublisherFactory.class);
    }
}
