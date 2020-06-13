package io.jenkins.plugins.github.checks.api;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.Nullable;

import io.jenkins.plugins.github.checks.ChecksConclusion;
import io.jenkins.plugins.github.checks.ChecksStatus;

import static java.util.Objects.requireNonNull;

/**
 * TODO: Add JavaDoc when this class's structure is finally determined.
 */
public class ChecksDetails {
    private final String name;
    private final ChecksStatus status;
    private final String detailsURL;
    private final ChecksConclusion conclusion;
    private final ChecksOutput output;
    private final List<Action> actions;

    private ChecksDetails(final String name, final ChecksStatus status, final String detailsURL,
            final ChecksConclusion conclusion, final ChecksOutput output, final List<Action> actions) {
        this.name = name;
        this.status = status;
        this.detailsURL = detailsURL;
        this.conclusion = conclusion;
        this.output = output;
        this.actions = actions;
    }

    /**
     * Returns the name of a check.
     *
     * @return the unique name of a check
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the status of a check.
     *
     * @return {@link ChecksStatus}, one of {@code QUEUED}, {@code IN_PROGRESS}, {@code COMPLETED}
     */
    public ChecksStatus getStatus() {
        return status;
    }

    /**
     * Returns the url of a site with full details of a check.
     *
     * @return the url of a site or null
     */
    @Nullable
    public String getDetailsURL() {
        return detailsURL;
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
     * Returns the {@link ChecksOutput} of a check
     *
     * @return An {@link ChecksOutput} of a check or null
     */
    @Nullable
    public ChecksOutput getOutput() {
        return output;
    }

    /**
     * Returns the {@link Action}s of a check
     *
     * @return An immutable list of {@link Action}s of a check
     */
    public List<Action> getActions() {
        return actions;
    }

    public static class ChecksDetailsBuilder {
        private final String name;
        private final ChecksStatus status;
        private String detailsURL;
        private ChecksConclusion conclusion;
        private ChecksOutput output;
        private List<Action> actions;

        /**
         * Construct a builder with the given name and status.
         *
         * <p>
         *     The name will be the same as the check run's name shown on GitHub UI and GitHub uses this name to
         *     identify a check run, so make sure this name is unique, e.g. "Coverage".
         * <p>
         *
         * @param name
         *         the name of the check run
         * @param status
         *         the status which the check run will be set
         *
         * @throws IllegalArgumentException if the name is blank or the status is null
         */
        public ChecksDetailsBuilder(final String name, final ChecksStatus status) {
            requireNonNull(status);
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("check name should not be blank");
            }

            this.name = name;
            this.status = status;

            conclusion = ChecksConclusion.NONE;
            actions = Collections.emptyList();
        }

        /**
         * Set the url of a site with full details of a check.
         *
         * <p>
         *     If the details url is not set, the Jenkins build url will be used,
         *     e.g. ci.jenkins.io/job/Core/job/jenkins/job/master/2000/.
         * </p>
         *
         * @param detailsURL
         *         the url of the site which shows the detail information of the check
         * @return this builder
         * @throws NullPointerException if the {@code detailsURL} is null
         */
        public ChecksDetailsBuilder withDetailsURL(final String detailsURL) {
            requireNonNull(detailsURL);
            this.detailsURL = detailsURL;
            return this;
        }

        /**
         * Set the conclusion of a check.
         *
         * <p>
         *     The conclusion should only be set when the {@code status} was set {@link ChecksStatus#COMPLETED}
         *     when constructing this builder.
         * </p>
         *
         * @param conclusion
         *         the conclusion
         * @return this builder
         * @throws NullPointerException if the {@code conclusion} is null
         * @throws IllegalArgumentException if the {@code status} is not {@link ChecksStatus#COMPLETED}
         */
        public ChecksDetailsBuilder withConclusion(final ChecksConclusion conclusion) {
            requireNonNull(conclusion);

            if (status != ChecksStatus.COMPLETED) {
                throw new IllegalArgumentException("status must be completed when setting conclusion");
            }
            this.conclusion = conclusion;
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
            // TODO: Should store the clone of the output after output is constructed.
            requireNonNull(output);
            this.output = output;
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
        public ChecksDetailsBuilder withActions(final List<Action> actions) {
            requireNonNull(actions);
            this.actions = Collections.unmodifiableList(actions);
            return this;
        }

        /**
         * Actually build the {@code ChecksDetail}.
         *
         * @return the built {@code ChecksDetail}
         * @throws IllegalArgumentException if {@code conclusion} is null when {@code status} is {@code completed}
         */
        public ChecksDetails build() {
            if (conclusion == ChecksConclusion.NONE && status == ChecksStatus.COMPLETED) {
                throw new IllegalArgumentException("conclusion must be set when status is completed");
            }

            return new ChecksDetails(name, status, detailsURL, conclusion, output, actions);
        }
    }
}

