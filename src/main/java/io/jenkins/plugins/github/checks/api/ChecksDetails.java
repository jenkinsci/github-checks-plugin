package io.jenkins.plugins.github.checks.api;

import java.util.List;

public class ChecksDetails {
    private final String name;
    private final String detailsURL;
    private final String conclusion;
    private final List<Output> outputs;
    private final List<Action> actions;

    private ChecksDetails(final String name, final String detailsURL, final String conclusion,
            final List<Output> outputs, final List<Action> actions) {
        this.name = name;
        this.detailsURL = detailsURL;
        this.conclusion = conclusion;
        this.outputs = outputs;
        this.actions = actions;
    }

    public String getName() {
        return name;
    }

    public String getDetailsURL() {
        return detailsURL;
    }

    public String getConclusion() {
        return conclusion;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public List<Action> getActions() {
        return actions;
    }

    public static class ChecksDetailBuilder {
        private final String name;
        private String detailsURL;
        private String conclusion;
        private List<Output> outputs;
        private List<Action> actions;

        public ChecksDetailBuilder(String name) {
            this.name = name;
        }

        public ChecksDetailBuilder withDetailsURL(final String detailsURL) {
            this.detailsURL = detailsURL;
            return this;
        }

        public ChecksDetailBuilder withConclusion(final String conclusion) {
            this.conclusion = conclusion;
            return this;
        }

        public ChecksDetailBuilder withOutputs(final List<Output> outputs) {
            this.outputs = outputs;
            return this;
        }

        public ChecksDetailBuilder withActions(final List<Action> actions) {
            this.actions = actions;
            return this;
        }

        public ChecksDetails build() {
            return new ChecksDetails(name, detailsURL, conclusion, outputs, actions);
        }
    }
}

