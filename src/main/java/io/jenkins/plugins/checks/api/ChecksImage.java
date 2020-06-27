package io.jenkins.plugins.checks.api;

import static java.util.Objects.*;

/**
 * An image of a check. Users may use a image to show the code coverage, issues trend, etc.
 */
public class ChecksImage {
    private final String alt;
    private final String imageUrl;
    private String caption;

    /**
     * Construct an image with required parameters.
     *
     * @param alt
     *         the alternative text for the image
     * @param imageUrl
     *         the full URL of the image
     */
    public ChecksImage(final String alt, final String imageUrl) {
        this.alt = requireNonNull(alt);
        this.imageUrl = requireNonNull(imageUrl);
    }

    /**
     * Copy constructor.
     *
     * @param that
     *         the source
     */
    public ChecksImage(final ChecksImage that) {
        this(that.getAlt(), that.getImageUrl());
        this.caption = that.getCaption();
    }

    /**
     * Returns the alternative text for the image.
     *
     * @return the alternative text for the image
     */
    public String getAlt() {
        return alt;
    }

    /**
     * Returns the image URL.
     *
     * @return the image URL
     */
    public String getImageUrl() {
        // TODO: determine if the image URL should http or https scheme
        return imageUrl;
    }

    /**
     * Returns the short description of the image
     *
     * @return the short description of the image
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Set the short description for the image
     *
     * @param caption
     *         A short image description
     * @return this image
     */
    public ChecksImage withCaption(final String caption) {
        this.caption = requireNonNull(caption);
        return this;
    }
}
