package io.jenkins.plugins.checks.api;

import java.util.Optional;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;

/**
 * An image of a check. Users may use a image to show the code coverage, issues trend, etc.
 */
@Restricted(Beta.class)
public class ChecksImage {
    private final String alt;
    private final String imageUrl;
    private final String caption;

    /**
     * Constructs an image with all parameters.
     *
     * @param alt
     *         the alternative text for the image
     * @param imageUrl
     *         the full URL of the image
     * @param caption
     *         a short description of the image
     */
    public ChecksImage(@Nullable final String alt, @Nullable final String imageUrl, @Nullable final String caption) {
        this.alt = alt;
        this.imageUrl = imageUrl;
        this.caption = caption;
    }

    /**
     * Returns the alternative text for the image.
     *
     * @return the alternative text for the image
     */
    public Optional<String> getAlt() {
        return Optional.ofNullable(alt);
    }

    /**
     * Returns the image URL.
     *
     * @return the image URL
     */
    public Optional<String> getImageUrl() {
        return Optional.ofNullable(imageUrl);
    }

    /**
     * Returns the short description of the image.
     *
     * @return the short description of the image
     */
    public Optional<String> getCaption() {
        return Optional.ofNullable(caption);
    }
}
