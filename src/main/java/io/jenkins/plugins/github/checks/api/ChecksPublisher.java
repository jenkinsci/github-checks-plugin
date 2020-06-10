package io.jenkins.plugins.github.checks.api;

import java.io.IOException;
import java.util.List;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;
import hudson.ExtensionPoint;
import hudson.model.Run;

import io.jenkins.plugins.github.checks.ChecksContext;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * A publisher API for consumers to publish checks.
 */
@Restricted(Beta.class)
public abstract class ChecksPublisher implements ExtensionPoint {
    protected ChecksContext context; // not thread-safe?

    /**
     * Publishes checks to platforms.
     *
     * @param details
     *         the details of a check
     * @throws IOException if publish check failed
     */
    public abstract void publish(final ChecksDetails details) throws IOException;

    protected abstract boolean isApplicable(ChecksContext context);

    /**
     * Returns a suitable publisher for the run.
     *
     * @param run
     *         a Jenkins build
     * @return a publisher suitable for the run
     */
    public static ChecksPublisher fromRun(final Run<?, ?> run) {
        ChecksContext context = new ChecksContext(run);
        for (ChecksPublisher publisher : findAllPublishers()) {
            if (publisher.isApplicable(context)) {
                publisher.context = context;
                return publisher;
            }
        }
        return new NullChecksPublisher();
    }

    private static List<ChecksPublisher> findAllPublishers() {
        return new JenkinsFacade().getExtensionsFor(ChecksPublisher.class);
    }

    public static class NullChecksPublisher extends ChecksPublisher {
        @Override
        public void publish(final ChecksDetails details) {
        }

        @Override
        protected boolean isApplicable(final ChecksContext context) {
            return false;
        }
    }
}
