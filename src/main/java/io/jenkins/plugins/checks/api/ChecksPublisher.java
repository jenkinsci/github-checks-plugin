package io.jenkins.plugins.checks.api;

import hudson.model.Run;

public abstract class ChecksPublisher {
    protected Run<?, ?> run;

    public ChecksPublisher(final Run<?, ?> run) {
        this.run = run;
    }
    /**
     * Publishes checks to platforms.
     *
     * @param details
     *         the details of a check
     */
    public abstract void publish(final ChecksDetails details);

    public static class NullChecksPublisher extends ChecksPublisher {
        public NullChecksPublisher(final Run<?, ?> run) {
            super(run);
        }

        @Override
        public void publish(final ChecksDetails details) {
        }
    }
}
