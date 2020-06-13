package io.jenkins.plugins.github.checks.api;

import java.util.Objects;

import org.kohsuke.github.GHCheckRun;
import org.kohsuke.github.GHCheckRunBuilder.Output;

/**
 * A simple wrapper for {@link GHCheckRun.Output} to create an output for the GitHub's check run.
 */
public class ChecksOutput {
    private final Output output;

    private ChecksOutput(final Output output) {
        this.output = output;
    }

    public Output toGitHubChecksOutput() {
        return output;
    }

    public static class ChecksOutputBuilder {
        private final Output output;

        public ChecksOutputBuilder(final String title, final String summary) {
            Objects.requireNonNull(title);
            Objects.requireNonNull(summary);
            output = new Output(title, summary);
        }

        public ChecksOutputBuilder withText(final String text) {
            Objects.requireNonNull(text);
            output.withText(text);
            return this;
        }

        public ChecksOutputBuilder add(final ChecksAnnotation annotation) {
            Objects.requireNonNull(annotation);
            output.add(annotation.toGitHubChecksAnnotation());
            return this;
        }

        public ChecksOutput create() {
            return new ChecksOutput(output);
        }

        // TODO: Add image object
    }
}
