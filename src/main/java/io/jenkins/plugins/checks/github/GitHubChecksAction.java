package io.jenkins.plugins.checks.github;

import hudson.model.InvisibleAction;
import io.jenkins.plugins.checks.api.ChecksConclusion;

public class GitHubChecksAction extends InvisibleAction {

    private final long id;
    private final String name;
    private ChecksConclusion conclusion;

    public GitHubChecksAction(long id, String name, ChecksConclusion conclusion) {
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

    public void setConclusion(ChecksConclusion conclusion) {
        this.conclusion = conclusion;
    }

}
