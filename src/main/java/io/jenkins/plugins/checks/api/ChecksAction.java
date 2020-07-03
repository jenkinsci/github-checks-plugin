package io.jenkins.plugins.checks.api;

import java.util.Optional;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;

/**
 * An action of a check. It can be used to create actions like re-run or automatic formatting.
 */
@Restricted(Beta.class)
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
    public ChecksAction(@Nullable final String label, @Nullable final String description,
            @Nullable final String identifier) {
        this.label = label;
        this.description = description;
        this.identifier = identifier;
    }

    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<String> getIdentifier() {
        return Optional.ofNullable(identifier);
    }
}
