package io.jenkins.plugins.checks.api;

public class ChecksImage {
    private final String alt;
    private final String imageUrl;
    private String caption;

    public ChecksImage(final String alt, final String imageUrl) {
        this.alt = alt;
        this.imageUrl = imageUrl;
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

    public ChecksImage withCaption(final String caption) {
        this.caption = caption;
        return this;
    }
}
