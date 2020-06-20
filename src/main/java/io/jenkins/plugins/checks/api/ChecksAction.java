package io.jenkins.plugins.checks.api;

import static java.util.Objects.*;

public class ChecksAction {
    private final String label;
    private final String description;
    private final String identifier;

    /**
     * Creates a {@link ChecksAction} using the given parameters.
     *
     * <p>
     *     Note that for a GitHub check run, the {@code label}, {@code description}, and {@code identifier} must not
     *     exceed 20, 40, and 20 characters.
     * </p>
     *
     * @param label
     *         the text to be displayed on a button in web UI
     * @param description
     *         a short explanation of what this action would do
     * @param identifier
     *         a reference for the action on the integrator's system
     */
    public ChecksAction(final String label, final String description, final String identifier) {
        this.label = requireNonNull(label);
        this.description = requireNonNull(description);
        this.identifier = requireNonNull(identifier);
    }

    /**
     * Copy constructor of the {@link ChecksOutput}.
     *
     * @param that
     *         the source to copy from
     */
    public ChecksAction(final ChecksAction that) {
        this(that.getLabel(), that.getDescription(), that.getIdentifier());
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getIdentifier() {
        return identifier;
    }
}
