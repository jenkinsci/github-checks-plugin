package io.jenkins.plugins.checks.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;

import static java.util.Objects.requireNonNull;

/**
 * Details of a check. This class is a top class which contains all parameters needed for a check.
 */
@Restricted(Beta.class)
public class ChecksDetails {
    private final String name;
    private final ChecksStatus status;
    private final String detailsURL;
    private final LocalDateTime startedAt;
    private final ChecksConclusion conclusion;
    private final LocalDateTime completedAt;
    private final ChecksOutput output;
    private final List<ChecksAction> actions;

    private ChecksDetails(final String name, final ChecksStatus status, final String detailsURL,
            final LocalDateTime startedAt, final ChecksConclusion conclusion, final LocalDateTime completedAt,
            final ChecksOutput output, final List<ChecksAction> actions) {
        this.name = name;
        this.status = status;
        this.detailsURL = detailsURL;
        this.startedAt = startedAt;
        this.conclusion = conclusion;
        this.completedAt = completedAt;
        this.output = output;
        this.actions = actions;
    }

    /**
     * Returns the name of a check.
     *
     * @return the unique name of a check
     */
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    /**
     * Returns the status of a check.
     *
     * @return {@link ChecksStatus}, one of {@code QUEUED}, {@code IN_PROGRESS}, {@code COMPLETED}.
     */
    public ChecksStatus getStatus() {
        return status;
    }

    /**
     * Returns the url of a site with full details of a check.
     *
     * @return the url of a site
     */
    public Optional<String> getDetailsURL() {
        return Optional.ofNullable(detailsURL);
    }

    /**
     * Returns the time that the check started.
     *
     * @return the start time of a check
     */
    public Optional<LocalDateTime> getStartedAt() {
        return Optional.ofNullable(startedAt);
    }

    /**
     * Returns the conclusion of a check.
     *
     * @return the conclusion of a check
     */
    public ChecksConclusion getConclusion() {
        return conclusion;
    }

    /**
     * Returns the time that the check completed.
     *
     * @return the complete time of a check
     */
    public Optional<LocalDateTime> getCompletedAt() {
        return Optional.ofNullable(completedAt);
    }

    /**
     * Returns the {@link ChecksOutput} of a check.
     *
     * @return An {@link ChecksOutput} of a check
     */
    public Optional<ChecksOutput> getOutput() {
        return Optional.ofNullable(output);
    }

    /**
     * Returns the {@link ChecksAction}s of a check.
     *
     * @return An immutable list of {@link ChecksAction}s of a check
     */
    public List<ChecksAction> getActions() {
        return actions;
    }

    /**
     * Builder for {@link ChecksDetails}.
     */
    public static class ChecksDetailsBuilder {
        private String name;
        private ChecksStatus status;
        private String detailsURL;
        private LocalDateTime startedAt;
        private ChecksConclusion conclusion;
        private LocalDateTime completedAt;
        private ChecksOutput output;
        private List<ChecksAction> actions;

        /**
         * Construct a builder for {@link ChecksDetails}.
         */
        public ChecksDetailsBuilder() {
            this.conclusion = ChecksConclusion.NONE;
            this.actions = new ArrayList<>();
        }

        /**
         * Set the name of the check.
         *
         * <p>
         *     Note that for GitHub check runs, the name shown on GitHub UI will be the same as this attribute and
         *     GitHub uses this attribute to identify a check run, so make sure this name is unique, e.g. "Coverage".
         * <p>
         *
         * @param name
         *         the check's name
         * @return this builder
         * @throws NullPointerException if the {@code name} is null
         */
        public ChecksDetailsBuilder withName(final String name) {
            this.name = requireNonNull(name);
            return this;
        }

        /**
         * Set the status of the check.
         *
         * <p>
         *     Note that for a GitHub check run, if the status is not set, the default "queued" will be used.
         * </p>
         *
         * @param status
         *         the check's status
         * @return this builder
         * @throws NullPointerException if the {@code status} is null
         */
        public ChecksDetailsBuilder withStatus(final ChecksStatus status) {
            this.status = requireNonNull(status);
            return this;
        }

        /**
         * Set the url of a site with full details of a check.
         *
         * <p>
         *     Note that for a GitHub check run, the url must use http or https scheme.
         * </p>
         *
         * <p>
         *     If the details url is not set, the Jenkins build url will be used,
         *     e.g. https://ci.jenkins.io/job/Core/job/jenkins/job/master/2000/.
         * </p>
         *
         * @param detailsURL
         *         the url using http or https scheme
         * @return this builder
         * @throws NullPointerException if the {@code detailsURL} is null
         */
        public ChecksDetailsBuilder withDetailsURL(final String detailsURL) {
            this.detailsURL = requireNonNull(detailsURL);
            return this;
        }

        /**
         * Set the time when a check starts.
         *
         * @param startedAt
         *         the time when a check starts
         * @return this builder
         * @throws NullPointerException if the {@code startAt} is null
         */
        public ChecksDetailsBuilder withStartedAt(final LocalDateTime startedAt) {
            this.startedAt = requireNonNull(startedAt);
            return this;
        }

        /**
         * Set the conclusion of a check.
         *
         * <p>
         *     Note that for a GitHub check run, the conclusion should only be set when the {@code status}
         *     was {@link ChecksStatus#COMPLETED}.
         * </p>
         *
         * @param conclusion
         *         the conclusion
         * @return this builder
         * @throws NullPointerException if the {@code conclusion} is null
         */
        public ChecksDetailsBuilder withConclusion(final ChecksConclusion conclusion) {
            this.conclusion = requireNonNull(conclusion);
            return this;
        }

        /**
         * Set the time when a check completes.
         *
         * @param completedAt
         *         the time when a check completes
         * @return this builder
         * @throws NullPointerException if the {@code completedAt} is null
         */
        public ChecksDetailsBuilder withCompletedAt(final LocalDateTime completedAt) {
            this.completedAt = requireNonNull(completedAt);
            return this;
        }

        /**
         * Set the output of a check.
         *
         * @param output
         *         an output of a check
         * @return this builder
         * @throws NullPointerException if the {@code outputs} is null
         */
        public ChecksDetailsBuilder withOutput(final ChecksOutput output) {
            this.output = new ChecksOutput(requireNonNull(output));
            return this;
        }

        /**
         * Set the actions of a check.
         *
         * @param actions
         *         a list of actions
         * @return this builder
         * @throws NullPointerException if the {@code actions} is null
         */
        public ChecksDetailsBuilder withActions(final List<ChecksAction> actions) {
            this.actions = new ArrayList<>(requireNonNull(actions));
            return this;
        }

        /**
         * Adds a {@link ChecksAction}.
         *
         * @param action
         *         the action
         * @return this builder
         */
        public ChecksDetailsBuilder addAction(final ChecksAction action) {
            actions.add(requireNonNull(action));
            return this;
        }

        /**
         * Actually build the {@code ChecksDetail}.
         *
         * @return the built {@code ChecksDetail}
         */
        public ChecksDetails build() {
            return new ChecksDetails(name, status, detailsURL, startedAt, conclusion, completedAt, output,
                    Collections.unmodifiableList(actions));
        }
    }
}
