package io.jenkins.plugins.checks.github;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.github.GHCheckRun.AnnotationLevel;
import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GHCheckRunBuilder.Action;
import org.kohsuke.github.GHCheckRunBuilder.Image;
import org.kohsuke.github.GHCheckRunBuilder.Output;
import org.kohsuke.github.GHCheckRunBuilder.Annotation;

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

    /**
     * Construct with the given {@link ChecksDetails}.
     *
     * @param details the details of a generic check run
     */
    public GitHubChecksDetails(final ChecksDetails details) {
        if (details.getStatus() == ChecksStatus.COMPLETED && details.getConclusion() == ChecksConclusion.NONE) {
            throw new IllegalArgumentException("The conclusion is null when status is completed.");
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
                .filter(name -> !StringUtils.isBlank(name))
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
            case QUEUED:
                return Status.QUEUED;
            case IN_PROGRESS:
                return Status.IN_PROGRESS;
            case COMPLETED:
                return Status.COMPLETED;
            case NONE:
                throw new IllegalArgumentException("Status is null.");
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
        details.getDetailsURL()
                .ifPresent(url -> {
                    if (!StringUtils.equalsAny(URI.create(url).getScheme(), "http", "https")) {
                        throw new IllegalArgumentException("The details url is not http or https scheme: " + url);
                    }
                });
        return details.getDetailsURL();
    }

    /**
     * Returns the time when the check started.
     *
     * @return the start time of a check
     */
    public LocalDateTime getStartedAt() {
        return details.getStartedAt()
                .orElse(LocalDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Returns the {@link Conclusion} of a completed GitHub check run.
     *
     * @return the conclusion of a completed check run
     * @throws IllegalArgumentException if the conclusion of the {@code details} is not one of {@link ChecksConclusion}
     */
    public Optional<Conclusion> getConclusion() {
        switch (details.getConclusion()) {
            case SKIPPED: // TODO: Open a PR to add SKIPPED in Conclusion
            case CANCELED:
                return Optional.of(Conclusion.CANCELLED);
            case TIME_OUT:
                return Optional.of(Conclusion.TIMED_OUT);
            case FAILURE:
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
     * Returns the time when the check completed.
     *
     * @return the completed time of a check
     */
    public LocalDateTime getCompletedAt() {
        return details.getCompletedAt()
                .orElse(LocalDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Returns the {@link Output} of a GitHub check run.
     *
     * @return the output of a check run or null
     */
    public Optional<Output> getOutput() {
        Output output = null;
        if (details.getOutput().isPresent()) {
            ChecksOutput checksOutput = details.getOutput().get();
            output = new Output(checksOutput.getTitle(), checksOutput.getSummary())
                    .withText(checksOutput.getText());
            checksOutput.getChecksAnnotations().stream().map(this::getAnnotation).forEach(output::add);
            checksOutput.getChecksImages().stream().map(this::getImage).forEach(output::add);
        }

        return Optional.ofNullable(output);
    }

    public List<Action> getActions() {
        return details.getActions().stream()
                .map(this::getAction)
                .collect(Collectors.toList());
    }

    private Action getAction(final ChecksAction checksAction) {
        if (details.getActions().size() > 3) {
            throw new IllegalArgumentException(String.format(
                    "A maximum of three actions are supported, but %d are provided.",
                    details.getActions().size()));
        }

        for (ChecksAction action : details.getActions()) {
            if (action.getLabel().length() > 20) {
                throw new IllegalArgumentException("The action's label exceeds the maximum 20 characters: "
                        + action.getLabel());
            }

            if (action.getDescription().length() > 40) {
                throw new IllegalArgumentException("The action's description exceeds the maximum 40 characters: "
                        + action.getDescription());
            }

            if (action.getIdentifier().length() > 20) {
                throw new IllegalArgumentException("The action's identifier exceeds the maximum 20 characters: "
                        + action.getIdentifier());
            }
        }

        return new Action(checksAction.getLabel(), checksAction.getDescription(), checksAction.getIdentifier());
    }

    private Annotation getAnnotation(final ChecksAnnotation checksAnnotation) {
        return new Annotation(checksAnnotation.getPath(),
                checksAnnotation.getStartLine(), checksAnnotation.getEndLine(),
                getAnnotationLevel(checksAnnotation.getAnnotationLevel()),
                checksAnnotation.getMessage())
                .withTitle(checksAnnotation.getTitle())
                .withRawDetails(checksAnnotation.getRawDetails())
                .withStartColumn(checksAnnotation.getStartColumn())
                .withEndColumn(checksAnnotation.getEndColumn());
    }

    private Image getImage(final ChecksImage checksImage) {
        return new Image(checksImage.getAlt(), checksImage.getImageUrl()).withCaption(checksImage.getCaption());
    }

    private AnnotationLevel getAnnotationLevel(final ChecksAnnotationLevel checksLevel) {
        switch (checksLevel) {
            case NOTICE: return AnnotationLevel.NOTICE;
            case FAILURE: return AnnotationLevel.FAILURE;
            case WARNING: return AnnotationLevel.WARNING;
            default: throw new IllegalArgumentException("unsupported checks annotation level: " + checksLevel);
        }
    }
}
