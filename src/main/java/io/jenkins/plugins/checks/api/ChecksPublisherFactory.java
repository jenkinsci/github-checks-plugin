package io.jenkins.plugins.checks.api;

import java.util.List;
import java.util.Optional;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;
import hudson.ExtensionPoint;
import hudson.model.Run;

import io.jenkins.plugins.checks.ChecksContext;
import io.jenkins.plugins.checks.api.ChecksPublisher.NullChecksPublisher;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * A publisher API for consumers to publish checks.
 */
@Restricted(Beta.class)
public abstract class ChecksPublisherFactory implements ExtensionPoint {
    protected abstract Optional<ChecksPublisher> createPublisher(final ChecksContext context);

    /**
     * Returns a suitable publisher for the run.
     *
     * @param run
     *         a Jenkins build
     * @return a publisher suitable for the run
     */
    public static ChecksPublisher fromRun(final Run<?, ?> run) {
        ChecksContext context = new ChecksContext(run);
        return findAllPublisherFactories().stream()
                .map(factory -> factory.createPublisher(context))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(new NullChecksPublisher(context));
    }

    private static List<ChecksPublisherFactory> findAllPublisherFactories() {
        return new JenkinsFacade().getExtensionsFor(ChecksPublisherFactory.class);
    }
}
