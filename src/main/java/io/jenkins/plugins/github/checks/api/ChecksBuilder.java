package io.jenkins.plugins.github.checks.api;

import java.util.List;

public class ChecksBuilder {
    private String detailsURL;
    private String conclusion;
    private List<Output> outputs;
    private List<Action> actions;
    final private String name;

    public ChecksBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDetailsURL() {
        return detailsURL;
    }

    public void setDetailsURL(final String detailsURL) {
        this.detailsURL = detailsURL;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(final String conclusion) {
        this.conclusion = conclusion;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(final List<Output> outputs) {
        this.outputs = outputs;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(final List<Action> actions) {
        this.actions = actions;
    }
}

