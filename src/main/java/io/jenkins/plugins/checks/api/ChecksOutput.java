package io.jenkins.plugins.checks.api;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.*;

/**
 * An output of a check. The output usually contains the most useful information like summary, description,
 * annotations, etc.
 */
public class ChecksOutput {
    private final String title;
    private final String summary;
    private final String text;
    private final List<ChecksAnnotation> annotations;
    private final List<ChecksImage> images;

    private ChecksOutput(final String title, final String summary, final String text,
            final List<ChecksAnnotation> annotations, final List<ChecksImage> images) {
        this.title = title;
        this.summary = summary;
        this.text = text;
        this.annotations = annotations;
        this.images = images;
    }

    /**
     * Copy constructor of the {@link ChecksOutput}.
     * @param that
     *         the source to copy from
     */
    public ChecksOutput(final ChecksOutput that) {
        this(that.getTitle(), that.getSummary(), that.getText(), that.getChecksAnnotations(), that.getChecksImages());
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getText() {
        return text;
    }

    public List<ChecksAnnotation> getChecksAnnotations() {
        return annotations;
    }

    public List<ChecksImage> getChecksImages() {
        return images;
    }

    /**
     * Builder for {@link ChecksOutput}.
     */
    public static class ChecksOutputBuilder {
        private final String title;
        private final String summary;
        private String text;
        private List<ChecksAnnotation> annotations;
        private List<ChecksImage> images;

        /**
         * Construct a builder with given title and summary for a {@link ChecksOutput}.
         *
         * @param title
         *         the title of a {@link ChecksOutput}
         * @param summary
         *         the summary of a {@link ChecksOutput}
         */
        public ChecksOutputBuilder(final String title, final String summary) {
            this.title = requireNonNull(title);
            this.summary = requireNonNull(summary);
            this.annotations = Collections.emptyList();
            this.images = Collections.emptyList();
        }

        /**
         * Adds the details description for a check run. This parameter supports Markdown.
         *
         * @param text
         *         the details description in Markdown
         * @return this builder
         */
        public ChecksOutputBuilder withText(final String text) {
            this.text = requireNonNull(text);
            return this;
        }

        /**
         * Adds the {@link ChecksAnnotation} for a check run.
         *
         * @param annotations
         *         the annotations list
         * @return this builder
         */
        public ChecksOutputBuilder withAnnotations(final List<ChecksAnnotation> annotations) {
            requireNonNull(annotations);
            this.annotations = Collections.unmodifiableList(
                    annotations.stream()
                            .map(ChecksAnnotation::new)
                            .collect(Collectors.toList())
            );
            return this;
        }

        /**
         * Adds the {@link ChecksImage} for a check run.
         * @param images
         *         the images list
         * @return this builder
         */
        public ChecksOutputBuilder withImages(final List<ChecksImage> images) {
            requireNonNull(images);
            this.images = Collections.unmodifiableList(
                    images.stream()
                            .map(ChecksImage::new)
                            .collect(Collectors.toList())
            );
            return this;
        }

        /**
         * Actually builds the {@link ChecksOutput} with given parameters.
         *
         * @return the built {@link ChecksOutput}
         */
        public ChecksOutput build() {
            return new ChecksOutput(title, summary, text, annotations, images);
        }
    }
}
