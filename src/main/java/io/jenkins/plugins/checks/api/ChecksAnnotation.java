package io.jenkins.plugins.checks.api;

import java.util.Objects;

public class ChecksAnnotation {
    private final String path;
    private final int startLine;
    private final int endLine;
    private final ChecksAnnotationLevel annotationLevel;
    private final String message;
    private final int startColumn;
    private final int endColumn;
    private final String title;
    private final String rawDetails;

    private ChecksAnnotation(final String path,
            final int startLine, final int endLine,
            final ChecksAnnotationLevel annotationLevel,
            final String message,
            final int startColumn, final int endColumn,
            final String title,
            final String rawDetails) {
        this.path = path;
        this.startLine = startLine;
        this.endLine = endLine;
        this.annotationLevel = annotationLevel;
        this.message = message;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.title = title;
        this.rawDetails = rawDetails;
    }

    public String getPath() {
        return path;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public ChecksAnnotationLevel getAnnotationLevel() {
        return annotationLevel;
    }

    public String getMessage() {
        return message;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public String getTitle() {
        return title;
    }

    public String getRawDetails() {
        return rawDetails;
    }

    public enum ChecksAnnotationLevel {
        NOTICE,
        WARNING,
        FAILURE
    }

    public static class ChecksAnnotationBuilder {
        private final String path;
        private final int startLine;
        private final int endLine;
        private final ChecksAnnotationLevel annotationLevel;
        private final String message;
        private int startColumn;
        private int endColumn;
        private String title;
        private String rawDetails;

        public ChecksAnnotationBuilder(final String path,
                final int startLine, final int endLine,
                final ChecksAnnotationLevel annotationLevel,
                final String message) {
            Objects.requireNonNull(path);
            Objects.requireNonNull(startLine);
            Objects.requireNonNull(endLine);
            Objects.requireNonNull(annotationLevel);
            Objects.requireNonNull(message);

            this.path = path;
            this.startLine = startLine;
            this.endLine = endLine;
            this.annotationLevel = annotationLevel;
            this.message = message;
        }

        public ChecksAnnotationBuilder withStartColumn(final int startColumn) {
            if (startLine != endLine) {
                throw new IllegalArgumentException("startLine and endLine attributes should the same "
                        + "when adding column");
            }
            this.startColumn = startColumn;
            return this;
        }

        public ChecksAnnotationBuilder withEndColumn(final int endColumn) {
            if (startLine != endLine) {
                throw new IllegalArgumentException("startLine and endLine attributes should the same "
                        + "when adding column");
            }
            this.endColumn = endColumn;
            return this;
        }

        public ChecksAnnotationBuilder withTitle(final String title) {
            // TODO: determine how github behave when passing a empty string
            Objects.requireNonNull(title);
            this.title = title;
            return this;
        }

        public ChecksAnnotationBuilder withRawDetails(final String rawDetails) {
            // TODO: what should we do when rawDetails exceeded 64kb
            Objects.requireNonNull(rawDetails);
            this.rawDetails = rawDetails;
            return this;
        }

        public ChecksAnnotation build() {
            return new ChecksAnnotation(path, startLine, endLine, annotationLevel, message, startColumn, endColumn,
                    title, rawDetails);
        }
    }
}
