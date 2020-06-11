package io.jenkins.plugins.github.checks;

import java.io.IOException;

import io.jenkins.plugins.github.checks.api.ChecksDetails;

public abstract class ChecksPublisher {
    protected ChecksContext context;

    public ChecksPublisher(final ChecksContext context) {
        this.context = context;
    }
    /**
     * Publishes checks to platforms.
     *
     * @param details
     *         the details of a check
     * @throws IOException if publish check failed
     */
    public abstract void publish(final ChecksDetails details) throws IOException;

    public static class NullChecksPublisher extends ChecksPublisher {
        public NullChecksPublisher(final ChecksContext context) {
            super(context);
        }

        @Override
        public void publish(final ChecksDetails details) throws IOException {
        }
    }
}
