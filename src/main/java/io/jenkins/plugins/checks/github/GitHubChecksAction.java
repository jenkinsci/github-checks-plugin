package io.jenkins.plugins.checks.github;

import hudson.model.InvisibleAction;
import io.jenkins.plugins.checks.api.ChecksConclusion;

@SuppressWarnings("PMD.DataClass")
public class GitHubChecksAction extends InvisibleAction {

    private final long id;
    private final String name;
    private ChecksConclusion conclusion;

    public GitHubChecksAction(final long id, final String name, final ChecksConclusion conclusion) {
        super();
        this.id = id;
        this.name = name;
        this.conclusion = conclusion;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ChecksConclusion getConclusion() {
        return conclusion;
    }

    public void setConclusion(final ChecksConclusion conclusion) {
        this.conclusion = conclusion;
    }

}
