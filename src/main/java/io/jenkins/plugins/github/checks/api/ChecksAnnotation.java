package io.jenkins.plugins.github.checks.api;

import org.kohsuke.github.GHCheckRun.AnnotationLevel;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GHCheckRunBuilder.Annotation;

/**
 * A simple wrapper for {@link GHCheckRunBuilder.Annotation} to create an annotation for the GitHub's check run.
 */
public class ChecksAnnotation {
    private final Annotation annotation;

    private ChecksAnnotation(final Annotation annotation) {
        this.annotation = annotation;
    }

    public Annotation toGitHubChecksAnnotation() {
        return annotation;
    }

    public static class ChecksAnnotationBuilder {
        GHCheckRunBuilder.Annotation annotation;

        public ChecksAnnotationBuilder(final String path, final int line,
                final ChecksAnnotationLevel level, final String message) {
            annotation = new Annotation(path, line, level.toGitHubAnnotationLevel(), message);
        }


        public ChecksAnnotationBuilder withStartColumn(final int startColumn) {
            annotation.withStartColumn(startColumn);
            return this;
        }

        public ChecksAnnotationBuilder withEndColumn(final int endColumn) {
            annotation.withEndColumn(endColumn);
            return this;
        }

        public ChecksAnnotationBuilder withTitle(final String title) {
            annotation.withTitle(title);
            return this;
        }

        public ChecksAnnotationBuilder withRawDetails(final String rawDetails) {
            annotation.withRawDetails(rawDetails);
            return this;
        }

        public ChecksAnnotation create() {
            return new ChecksAnnotation(annotation);
        }
    }

    public static enum ChecksAnnotationLevel {
        NOTICE(AnnotationLevel.NOTICE),
        WARNING(AnnotationLevel.WARNING),
        FAILURE(AnnotationLevel.FAILURE);

        private final AnnotationLevel level;

        ChecksAnnotationLevel(final AnnotationLevel level) {
            this.level = level;
        }

        public AnnotationLevel toGitHubAnnotationLevel() {
            return level;
        }
    }
}
