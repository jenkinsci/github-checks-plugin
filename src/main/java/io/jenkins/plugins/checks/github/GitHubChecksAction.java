package io.jenkins.plugins.checks.github;

import hudson.model.InvisibleAction;

import static java.util.Objects.requireNonNull;

/**
 * An invisible action to track the state of GitHub Checks so that the publisher can update existing checks by the
 * same name, and report back to the checks api the state of a named check (without having to go and check GitHub
 * each time).
 */
@SuppressWarnings("PMD.DataClass")
public class GitHubChecksAction extends InvisibleAction {

    private final long id;
    private final String name;

    /**
     * Construct a {@link GitHubChecksAction} with the given details.
     *
     * @param id the id of the check run as reported by GitHub
     * @param name the name of the check
     */
    public GitHubChecksAction(final long id, final String name) {
        super();
        this.id = id;
        this.name = requireNonNull(name);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
