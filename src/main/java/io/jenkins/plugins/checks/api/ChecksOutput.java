package io.jenkins.plugins.checks.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    public static class ChecksOutputBuilder {
        private final String title;
        private final String summary;
        private String text;
        private List<ChecksAnnotation> annotations;
        private List<ChecksImage> images;

        public ChecksOutputBuilder(final String title, final String summary) {
            Objects.requireNonNull(title);
            Objects.requireNonNull(summary);
            this.title = title;
            this.summary = summary;
            this.annotations = Collections.emptyList();
            this.images = Collections.emptyList();
        }

        public ChecksOutputBuilder withText(final String text) {
            Objects.requireNonNull(text);
            this.text = text;
            return this;
        }

        public ChecksOutputBuilder withAnnotations(final List<ChecksAnnotation> annotations) {
            Objects.requireNonNull(annotations);
            this.annotations = Collections.unmodifiableList(new ArrayList<>(annotations));
            return this;
        }

        public ChecksOutputBuilder withImages(final List<ChecksImage> images) {
            Objects.requireNonNull(images);
            this.images = Collections.unmodifiableList(new ArrayList<>(images));
            return this;
        }

        public ChecksOutput build() {
            return new ChecksOutput(title, summary, text, annotations, images);
        }
    }
}
