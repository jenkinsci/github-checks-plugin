package io.jenkins.plugins.checks.github;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.kohsuke.github.GHCheckRun;
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
        this.details = details;
    }

    /**
     * Returns the name of a GitHub check run.
     *
     * @return the name of the check
     */
    public String getName() {
        return details.getName();
    }

    /**
     * Returns the {@link GHCheckRun.Status} of a GitHub check run.
     *
     *
     * @return the status of a check run
     * @throws IllegalArgumentException if the status of the {@code details} is not one of {@link ChecksStatus}
     */
    public Status getStatus() {
        switch (details.getStatus()) {
            case QUEUED: return Status.QUEUED;
            case IN_PROGRESS: return Status.IN_PROGRESS;
            case COMPLETED: return Status.COMPLETED;
            default: throw new IllegalArgumentException("unsupported checks conclusion: " + details.getStatus());
        }
    }

    /**
     * Returns the URL of site which contains details of a GitHub check run.
     *
     * @return an URL of the site
     */
    public String getDetailsURL() {
        return details.getDetailsURL();
    }

    /**
     * Returns the time when the check started.
     *
     * @return the start time of a check
     */
    public LocalDateTime getStartedAt() {
        return details.getStartedAt();
    }

    /**
     * Returns the {@link GHCheckRun.Conclusion} of a completed GitHub check run.
     *
     * @return the conclusion of a completed check run
     * @throws IllegalArgumentException if the conclusion of the {@code details} is not one of {@link ChecksConclusion}
     */
    public Conclusion getConclusion() {
        switch (details.getConclusion()) {
            case SKIPPED: return Conclusion.CANCELLED; // TODO: Open a PR to add SKIPPED in Conclusion
            case TIME_OUT: return Conclusion.TIMED_OUT;
            case CANCELED: return Conclusion.CANCELLED;
            case FAILURE: return Conclusion.FAILURE;
            case NEUTRAL: return Conclusion.NEUTRAL;
            case SUCCESS: return Conclusion.SUCCESS;
            case ACTION_REQUIRED: return Conclusion.ACTION_REQUIRED;
            default: throw new IllegalArgumentException("unsupported checks conclusion: " + details.getConclusion());
        }
    }

    /**
     * Returns the time when the check completed.
     *
     * @return the completed time of a check
     */
    public LocalDateTime getCompletedAt() {
        return details.getCompletedAt();
    }

    /**
     * Returns the {@link GHCheckRunBuilder.Output} of a GitHub check run.
     *
     * @return the output of a check run or null
     */
    @CheckForNull
    public Output getOutput() {
        ChecksOutput checksOutput = details.getOutput();
        if (checksOutput == null) {
            return null;
        }

        Output output = new Output(checksOutput.getTitle(),
                checksOutput.getSummary());
        output.withText(checksOutput.getText());
        checksOutput.getChecksAnnotations().stream().map(this::getAnnotation).forEach(output::add);
        checksOutput.getChecksImages().stream().map(this::getImage).forEach(output::add);
        return output;
    }

    public List<Action> getActions() {
        return details.getActions().stream()
                .map(this::getAction)
                .collect(Collectors.toList());
    }

    private Action getAction(final ChecksAction checksAction) {
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
