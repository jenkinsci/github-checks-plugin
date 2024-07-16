package io.jenkins.plugins.checks.github;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.github.GHCheckRun.AnnotationLevel;
import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GHCheckRunBuilder.Action;
import org.kohsuke.github.GHCheckRunBuilder.Annotation;
import org.kohsuke.github.GHCheckRunBuilder.Image;
import org.kohsuke.github.GHCheckRunBuilder.Output;

import io.jenkins.plugins.checks.api.ChecksAction;
import io.jenkins.plugins.checks.api.ChecksAnnotation;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksImage;
import io.jenkins.plugins.checks.api.ChecksOutput;
import io.jenkins.plugins.checks.api.ChecksStatus;

/**
 * An adaptor which adapts the generic checks objects of {@link ChecksDetails} to the specific GitHub checks run
 * objects of {@link GHCheckRunBuilder}.
 */
class GitHubChecksDetails {
    private final ChecksDetails details;

    private static final int MAX_MESSAGE_SIZE_TO_CHECKS_API = 65_535;

    /**
     * Construct with the given {@link ChecksDetails}.
     *
     * @param details the details of a generic check run
     */
    GitHubChecksDetails(final ChecksDetails details) {
        if (details.getConclusion() == ChecksConclusion.NONE) {
            if (details.getStatus() == ChecksStatus.COMPLETED) {
                throw new IllegalArgumentException("No conclusion has been set when status is completed.");
            }

            if (details.getCompletedAt().isPresent()) {
                throw new IllegalArgumentException("No conclusion has been set when \"completedAt\" is provided.");
            }
        }

        this.details = details;
    }

    /**
     * Returns the name of a GitHub check run.
     *
     * @return the name of the check
     */
    public String getName() {
        return details.getName()
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new IllegalArgumentException("The check name is blank."));
    }

    /**
     * Returns the {@link Status} of a GitHub check run.
     *
     *
     * @return the status of a check run
     * @throws IllegalArgumentException if the status of the {@code details} is not one of {@link ChecksStatus}
     */
    public Status getStatus() {
        switch (details.getStatus()) {
            case NONE:
            case QUEUED:
                return Status.QUEUED;
            case IN_PROGRESS:
                return Status.IN_PROGRESS;
            case COMPLETED:
                return Status.COMPLETED;
            default:
                throw new IllegalArgumentException("Unsupported checks status: " + details.getStatus());
        }
    }

    /**
     * Returns the URL of site which contains details of a GitHub check run.
     *
     * @return an URL of the site
     */
    public Optional<String> getDetailsURL() {
        if (details.getDetailsURL().filter(StringUtils::isBlank).isPresent()) {
            return Optional.empty();
        }

        details.getDetailsURL().ifPresent(url -> {
            if (!StringUtils.equalsAny(URI.create(url).getScheme(), "http", "https")) {
                throw new IllegalArgumentException("The details url is not http or https scheme: " + url);
            }
        }
        );
        return details.getDetailsURL();
    }

    /**
     * Returns the UTC time when the check started.
     *
     * @return the start time of a check
     */
    public Optional<Date> getStartedAt() {
        if (details.getStartedAt().isPresent()) {
            return Optional.of(Date.from(
                    details.getStartedAt().get()
                            .toInstant(ZoneOffset.UTC)));
        }
        return Optional.empty();
    }

    /**
     * Returns the {@link Conclusion} of a completed GitHub check run.
     *
     * @return the conclusion of a completed check run
     * @throws IllegalArgumentException if the conclusion of the {@code details} is not one of {@link ChecksConclusion}
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Optional<Conclusion> getConclusion() {
        switch (details.getConclusion()) {
            case SKIPPED:
                return Optional.of(Conclusion.SKIPPED);
            case FAILURE:
            case CANCELED: // TODO use CANCELLED if https://github.com/github/feedback/discussions/10255 is fixed
            case TIME_OUT: // TODO TIMED_OUT as above
                return Optional.of(Conclusion.FAILURE);
            case NEUTRAL:
                return Optional.of(Conclusion.NEUTRAL);
            case SUCCESS:
                return Optional.of(Conclusion.SUCCESS);
            case ACTION_REQUIRED:
                return Optional.of(Conclusion.ACTION_REQUIRED);
            case NONE:
                return Optional.empty();
            default:
                throw new IllegalArgumentException("Unsupported checks conclusion: " + details.getConclusion());
        }
    }

    /**
     * Returns the UTC time when the check completed.
     *
     * @return the completed time of a check
     */
    public Optional<Date> getCompletedAt() {
        if (details.getCompletedAt().isPresent()) {
            return Optional.of(Date.from(
                    details.getCompletedAt().get()
                            .toInstant(ZoneOffset.UTC)));
        }
        return Optional.empty();
    }

    /**
     * Returns the {@link Output} of a GitHub check run.
     *
     * @return the output of a check run
     */
    public Optional<Output> getOutput() {
        if (details.getOutput().isPresent()) {
            ChecksOutput checksOutput = details.getOutput().get();
            Output output = new Output(
                    checksOutput.getTitle().orElseThrow(
                            () -> new IllegalArgumentException("Title of output is required but not provided")),
                    checksOutput.getSummary(MAX_MESSAGE_SIZE_TO_CHECKS_API).orElseThrow(
                            () -> new IllegalArgumentException("Summary of output is required but not provided")))
                    .withText(checksOutput.getText(MAX_MESSAGE_SIZE_TO_CHECKS_API).orElse(null));
            checksOutput.getChecksAnnotations().stream().map(this::getAnnotation).forEach(output::add);
            checksOutput.getChecksImages().stream().map(this::getImage).forEach(output::add);
            return Optional.of(output);
        }

        return Optional.empty();
    }

    /**
     * Returns the {@link Action} of a GitHub check run.
     *
     * @return the actions list of a check run.
     */
    public List<Action> getActions() {
        return details.getActions().stream()
                .map(this::getAction)
                .collect(Collectors.toList());
    }

    private Action getAction(final ChecksAction checksAction) {
        return new Action(
                checksAction.getLabel()
                        .orElseThrow(() ->
                                new IllegalArgumentException("Label of action is required but not provided")),
                checksAction.getDescription()
                        .orElseThrow(() ->
                                new IllegalArgumentException("Description of action is required but not provided")),
                checksAction.getIdentifier()
                        .orElseThrow(() ->
                                new IllegalArgumentException("Identifier of action is required but not provided")));
    }

    private Annotation getAnnotation(final ChecksAnnotation checksAnnotation) {
        return new Annotation(
                checksAnnotation.getPath()
                        .orElseThrow(() -> new IllegalArgumentException("Path is required but not provided.")),
                checksAnnotation.getStartLine()
                        .orElseThrow(() -> new IllegalArgumentException("Start line is required but not provided.")),
                checksAnnotation.getEndLine().
                        orElseThrow(() -> new IllegalArgumentException("End line is required but not provided.")),
                getAnnotationLevel(checksAnnotation.getAnnotationLevel()),
                checksAnnotation.getMessage()
                        .orElseThrow(() -> new IllegalArgumentException("Message is required but not provided.")))
                .withTitle(checksAnnotation.getTitle().orElse(null))
                .withRawDetails(checksAnnotation.getRawDetails().orElse(null))
                .withStartColumn(checksAnnotation.getStartColumn().orElse(null))
                .withEndColumn(checksAnnotation.getEndColumn().orElse(null));
    }

    private Image getImage(final ChecksImage checksImage) {
        return new Image(
                checksImage.getAlt()
                        .orElseThrow(() -> new IllegalArgumentException("alt of image is required but not provided.")),
                checksImage.getImageUrl()
                        .orElseThrow(() -> new IllegalArgumentException("url of image is required but not provided.")))
                .withCaption(checksImage.getCaption().orElse(null));
    }

    private AnnotationLevel getAnnotationLevel(final ChecksAnnotationLevel checksLevel) {
        switch (checksLevel) {
            case NOTICE:
                return AnnotationLevel.NOTICE;
            case FAILURE:
                return AnnotationLevel.FAILURE;
            case WARNING:
                return AnnotationLevel.WARNING;
            case NONE:
                throw new IllegalArgumentException("Annotation level is required but not set.");
            default:
                throw new IllegalArgumentException("Unsupported checks annotation level: " + checksLevel);
        }
    }
}
