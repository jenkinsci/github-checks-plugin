package io.jenkins.plugins.checks.api;

import java.util.Objects;

public class ChecksAction {
    private final String label;
    private final String description;
    private final String identifier;

    public ChecksAction(final String label, final String description, final String identifier) {
        Objects.requireNonNull(label);
        Objects.requireNonNull(description);
        Objects.requireNonNull(identifier);

        this.label = label;
        this.description = description;
        this.identifier = identifier;
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
