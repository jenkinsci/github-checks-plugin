package io.jenkins.plugins.github.checks.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;

import io.jenkins.plugins.github.checks.ChecksConclusion;
import io.jenkins.plugins.github.checks.ChecksStatus;

public class ChecksDetails {
    private final String name;
    private final ChecksStatus status;
    private final String detailsURL;
    private final ChecksConclusion conclusion;
    private final List<Output> outputs;
    private final List<Action> actions;

    private ChecksDetails(final String name, final ChecksStatus status, final String detailsURL,
            final ChecksConclusion conclusion, final List<Output> outputs, final List<Action> actions) {
        this.name = name;
        this.status = status;
        this.detailsURL = detailsURL;
        this.conclusion = conclusion;
        this.outputs = outputs;
        this.actions = actions;
    }

    /**
     * Returns the name of a check.
     *
     * @return the unique name of a check
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Returns the status of a check.
     *
     * @return {@link ChecksStatus}, one of {@code QUEUED}, {@code IN_PROGRESS}, {@code COMPLETED}
     */
    @NonNull
    public ChecksStatus getStatus() {
        return status;
    }

    /**
     * Returns the url of a site with full details of the check.
     *
     * @return the string representing the url of a site
     */
    @NonNull
    public Optional<String> getDetailsURL() {
        return Optional.ofNullable(detailsURL);
    }

    /**
     * Returns the conclusion of a check.
     *
     * @return {@link ChecksConclusion}, one of {@code SUCCESS}, {@code FAILURE}, {@code NEUTRAL}, {@code CANCELLED},
     *         {@code SKIPPED}, {@code TIME_OUT}, or {@code ACTION_REQUIRED} when {@link ChecksDetails#getStatus()}
     *         returns {@code COMPLETED}, otherwise an empty string
     */
    @NonNull
    public Optional<ChecksConclusion> getConclusion() {
        return Optional.ofNullable(conclusion);
    }

    /**
     * Returns the {@link Output}s of a check
     *
     * @return {@link Output}s of a check
     */
    @NonNull
    public Optional<List<Output>> getOutputs() {
        return Optional.ofNullable(outputs);
    }

    /**
     * Returns the {@link Action}s of a check
     *
     * @return {@link Action}s of a check
     */
    @NonNull
    public Optional<List<Action>> getActions() {
        return Optional.ofNullable(actions);
    }

    public static class ChecksDetailsBuilder {
        private final String name;
        private final ChecksStatus status;
        private String detailsURL;
        private ChecksConclusion conclusion;
        private List<Output> outputs;
        private List<Action> actions;

        public ChecksDetailsBuilder(@NonNull final String name, @NonNull final ChecksStatus status)
                throws IllegalArgumentException{
            Objects.requireNonNull(status);
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("check name should not be blank");
            }

            this.name = name;
            this.status = status;
        }

        @NonNull
        public ChecksDetailsBuilder withDetailsURL(@NonNull final String detailsURL) {
            Objects.requireNonNull(detailsURL);
            this.detailsURL = detailsURL;
            return this;
        }

        @NonNull
        public ChecksDetailsBuilder withConclusion(@NonNull final ChecksConclusion conclusion) {
            Objects.requireNonNull(conclusion);

            if (status != ChecksStatus.COMPLETED) {
                throw new IllegalArgumentException("status must be completed when setting conclusion");
            }
            this.conclusion = conclusion;
            return this;
        }

        @NonNull
        public ChecksDetailsBuilder withOutputs(@NonNull final List<Output> outputs) {
            Objects.requireNonNull(outputs);
            this.outputs = new ArrayList<>(outputs);
            return this;
        }

        @NonNull
        public ChecksDetailsBuilder withActions(@NonNull List<Action> actions) {
            Objects.requireNonNull(actions);
            this.actions = new ArrayList<>(actions);
            return this;
        }

        /**
         * Actually build the {@code ChecksDetail}.
         *
         * @return the built {@code ChecksDetail}
         * @throws IllegalArgumentException if {@code conclusion} is null when {@code status} is {@code completed}
         */
        public ChecksDetails build() throws IllegalArgumentException {
            if (conclusion == null && status == ChecksStatus.COMPLETED) {
                throw new IllegalArgumentException("conclusion must be set when status is completed");
            }

            return new ChecksDetails(name, status, detailsURL, conclusion, outputs, actions);
        }
    }
}

