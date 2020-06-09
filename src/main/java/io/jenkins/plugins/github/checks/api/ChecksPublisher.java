package io.jenkins.plugins.github.checks.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

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
    protected ChecksContext context;

    private static final Function<Optional<ChecksPublisher>, Stream<? extends ChecksPublisher>> OPTIONAL_MAPPER
            = o -> o.map(Stream::of).orElseGet(Stream::empty);

    /**
     * Publishes checks to platforms.
     *
     * @param details
     *         the details of a check
     * @throws IOException if publish check failed
     */
    public abstract void publish(final ChecksDetails details) throws IOException;

    /**
     * Actually creates a suitable publisher based on the {@code context}.
     *
     * @param context
     *         the context of a run
     * @return a publisher suitable for the context
     */
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
        return findAllPublishers().stream()
                .map(publisher -> publisher.createPublisher(context))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst()
                .orElse(new NullChecksPublisher());
    }

    private static List<ChecksPublisher> findAllPublishers() {
        return new JenkinsFacade().getExtensionsFor(ChecksPublisher.class);
    }

    public static class NullChecksPublisher extends ChecksPublisher {
        @Override
        public void publish(final ChecksDetails details) {
        }

        @Override
        protected Optional<ChecksPublisher> createPublisher(final ChecksContext context) {
            return Optional.empty();
        }
    }
}
