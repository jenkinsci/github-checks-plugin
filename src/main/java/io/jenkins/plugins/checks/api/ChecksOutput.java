package io.jenkins.plugins.checks.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;

import static java.util.Objects.*;

/**
 * An output of a check. The output usually contains the most useful information like summary, description,
 * annotations, etc.
 */
@Restricted(Beta.class)
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
     *
     * @param that
     *         the source to copy from
     */
    public ChecksOutput(final ChecksOutput that) {
        this(that.getTitle().orElse(null), that.getSummary().orElse(null), that.getText().orElse(null),
                that.getChecksAnnotations(), that.getChecksImages());
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<String> getSummary() {
        return Optional.ofNullable(summary);
    }

    public Optional<String> getText() {
        return Optional.ofNullable(text);
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
        private String title;
        private String summary;
        private String text;
        private List<ChecksAnnotation> annotations;
        private List<ChecksImage> images;

        /**
         * Construct a builder for a {@link ChecksOutput}.
         *
         */
        public ChecksOutputBuilder() {
            this.annotations = new ArrayList<>();
            this.images = new ArrayList<>();
        }

        /**
         * Sets the title of the check run.
         *
         * @param title
         *         the title of the check run
         * @return this builder
         */
        public ChecksOutputBuilder withTitle(final String title) {
            this.title = requireNonNull(title);
            return this;
        }

        /**
         * Sets the summary of the check run
         *
         * <p>
         *     Note that for the GitHub check runs, the {@code summary} supports Markdown.
         * <p>
         *
         * @param summary
         *         the summary of the check run
         * @return this builder
         */
        public ChecksOutputBuilder withSummary(final String summary) {
            this.summary = requireNonNull(summary);
            return this;
        }

        /**
         * Adds the details description for a check run. This parameter supports Markdown.
         *
         * <p>
         *     Note that for a GitHub check run, the {@code text} supports Markdown.
         * <p>
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
         * Sets the {@link ChecksAnnotation} for a check run.
         *
         * @param annotations
         *         the annotations list
         * @return this builder
         */
        public ChecksOutputBuilder withAnnotations(final List<ChecksAnnotation> annotations) {
            this.annotations = new ArrayList<>(requireNonNull(annotations));
            return this;
        }

        /**
         * Adds a {@link ChecksAnnotation}.
         *
         * @param annotation
         *         the annotation
         * @return this builder
         */
        public ChecksOutputBuilder addAnnotation(final ChecksAnnotation annotation) {
            annotations.add(new ChecksAnnotation(requireNonNull(annotation)));
            return this;
        }

        /**
         * Sets the {@link ChecksImage} for a check run.
         * @param images
         *         the images list
         * @return this builder
         */
        public ChecksOutputBuilder withImages(final List<ChecksImage> images) {
            this.images = new ArrayList<>(requireNonNull(images));
            return this;
        }

        /**
         * Adds a {@link ChecksImage}.
         *
         * @param image
         *         the image
         * @return this builder
         */
        public ChecksOutputBuilder addImage(final ChecksImage image) {
            images.add(requireNonNull(image));
            return this;
        }

        /**
         * Actually builds the {@link ChecksOutput} with given parameters.
         *
         * @return the built {@link ChecksOutput}
         */
        public ChecksOutput build() {
            return new ChecksOutput(title, summary, text,
                    Collections.unmodifiableList(annotations),
                    Collections.unmodifiableList(images));
        }
    }
}
