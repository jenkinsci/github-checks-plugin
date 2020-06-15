package io.jenkins.plugins.checks.api;

import java.util.Objects;

public class ChecksImage {
    private final String alt;
    private final String imageUrl;
    private String caption;

    public ChecksImage(final String alt, final String imageUrl) {
        Objects.requireNonNull(alt);
        Objects.requireNonNull(imageUrl);

        this.alt = alt;
        this.imageUrl = imageUrl;
    }

    public ChecksImage(final ChecksImage that) {
        this(that.getAlt(), that.getImageUrl());
        setCaption(that.getCaption());
    }

    public String getAlt() {
        return alt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(final String caption) {
        Objects.requireNonNull(caption);
        this.caption = caption;
    }
}
