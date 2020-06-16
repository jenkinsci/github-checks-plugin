package io.jenkins.plugins.checks.api;

import static java.util.Objects.*;

/**
 * The annotation for specific lines of code.
 */
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

    public ChecksAnnotation(final ChecksAnnotation that) {
        this(that.getPath(), that.getStartLine(), that.getEndLine(), that.getAnnotationLevel(), that.getMessage(),
                that.getStartColumn(), that.getEndColumn(), that.getTitle(), that.getRawDetails());
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

    /**
     * A builder for {@link ChecksAnnotation}.
     */
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

        /**
         * Constructs a builder with required parameters for a {@link ChecksAnnotation}.
         *
         * @param path
         *         the relative path of the file to annotation, e.g. assets/css/main.css
         * @param startLine
         *         the start line of the annotation
         * @param endLine
         *         the end line of the annotation
         * @param annotationLevel
         *         the level of the annotation, can be one of {@link ChecksAnnotationLevel#NOTICE},
         *         {@link ChecksAnnotationLevel#WARNING}, {@link ChecksAnnotationLevel#FAILURE}
         * @param message
         *         a short description of the feedback for the annotated code, must not exceed 64kb
         */
        public ChecksAnnotationBuilder(final String path, final int startLine, final int endLine,
                final ChecksAnnotationLevel annotationLevel, final String message) {
            requireNonNull(path);
            requireNonNull(annotationLevel);
            requireNonNull(message);

            this.path = path;
            this.startLine = startLine;
            this.endLine = endLine;
            this.annotationLevel = annotationLevel;
            this.message = message;
        }

        /**
         * Constructs a builder with required parameters for a {@link ChecksAnnotation}.
         *
         * @param path
         *         the relative path of the file to annotation, e.g. assets/css/main.css
         * @param line
         *         the line of the code to annotate
         * @param annotationLevel
         *         the level of the annotation, can be one of {@link ChecksAnnotationLevel#NOTICE},
         *         {@link ChecksAnnotationLevel#WARNING}, {@link ChecksAnnotationLevel#FAILURE}
         * @param message
         *         a short description of the feedback for the annotated code, must not exceed 64 KB
         */
        public ChecksAnnotationBuilder(final String path, final int line, final ChecksAnnotationLevel annotationLevel,
                final String message) {
            this(path, line, line, annotationLevel, message);
        }

        /**
         * Adds start column of the annotation.
         * TODO: determine how GitHub behaves when the start and end column are not provided at the same time
         *
         * @param startColumn
         *         the start column of the annotation
         * @return this builder
         */
        public ChecksAnnotationBuilder withStartColumn(final int startColumn) {
            if (startLine != endLine) {
                throw new IllegalArgumentException(String.format("startLine and endLine attributes must be the same "
                        + "when adding column, start line: %d, end line: %d", startLine, endLine));
            }
            this.startColumn = startColumn;
            return this;
        }

        /**
         * Adds end column of the annotation.
         *
         * @param endColumn
         *         the end column of the annotation
         * @return this builder
         */
        public ChecksAnnotationBuilder withEndColumn(final int endColumn) {
            if (startLine != endLine) {
                throw new IllegalArgumentException(String.format("startLine and endLine attributes must be the same "
                        + "when adding column, start line: %d, end line: %d", startLine, endLine));
            }
            this.endColumn = endColumn;
            return this;
        }

        /**
         * Adds the title that represents the annotation.
         *
         * @param title
         *         the title of the annotation, must not exceed 255 characters
         *         TODO: what to do when exceeding
         * @return this builder
         */
        public ChecksAnnotationBuilder withTitle(final String title) {
            requireNonNull(title);
            this.title = title;
            return this;
        }

        /**
         * Adds the details about this annotation.
         *
         * @param rawDetails
         *         the details about this annotation, must not exceed 64 KB
         * @return this builder
         */
        public ChecksAnnotationBuilder withRawDetails(final String rawDetails) {
            // TODO: what should we do when rawDetails exceeded 64kb
            requireNonNull(rawDetails);
            this.rawDetails = rawDetails;
            return this;
        }

        /**
         * Actually builds the {@link ChecksAnnotation}.
         *
         * @return the {@link ChecksAnnotation}
         */
        public ChecksAnnotation build() {
            return new ChecksAnnotation(path, startLine, endLine, annotationLevel, message, startColumn, endColumn,
                    title, rawDetails);
        }
    }
}
