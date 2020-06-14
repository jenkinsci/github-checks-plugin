package io.jenkins.plugins.checks.github;

import org.kohsuke.github.GHCheckRun.AnnotationLevel;
import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;
import org.kohsuke.github.GHCheckRunBuilder.Image;
import org.kohsuke.github.GHCheckRunBuilder.Output;
import org.kohsuke.github.GHCheckRunBuilder.Annotation;

import io.jenkins.plugins.checks.api.ChecksAnnotation;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksImage;
import io.jenkins.plugins.checks.api.ChecksOutput;

public class GitHubChecksDetails {
    private final ChecksDetails details;

    public GitHubChecksDetails(final ChecksDetails details) {
        this.details = details;
    }

    public String getName() {
        return details.getName();
    }

    public Status getStatus() {
        switch (details.getStatus()) {
            case QUEUED: return Status.QUEUED;
            case IN_PROGRESS: return Status.IN_PROGRESS;
            case COMPLETED: return Status.COMPLETED;
            default: return null;
        }
    }

    public String getDetailsURL() {
        return details.getDetailsURL();
    }

    public Conclusion getConclusion() {
        switch (details.getConclusion()) {
            case SKIPPED: return Conclusion.CANCELLED;
            case TIME_OUT: return Conclusion.TIMED_OUT;
            case CANCELED: return Conclusion.CANCELLED;
            case FAILURE: return Conclusion.FAILURE;
            case NEUTRAL: return Conclusion.NEUTRAL;
            case SUCCESS: return Conclusion.SUCCESS;
            case ACTION_REQUIRED: return Conclusion.ACTION_REQUIRED;
            default: return null;
        }
    }

    public Output getOutput() {
        ChecksOutput checksOutput = details.getOutput();
        Output output = new Output(checksOutput.getTitle(),
                checksOutput.getSummary());
        output.withText(checksOutput.getText());
        checksOutput.getChecksAnnotations().stream().map(this::getAnnotation).forEach(output::add);
        checksOutput.getChecksImages().stream().map(this::getImage).forEach(output::add);
        return output;
    }

    private Annotation getAnnotation(final ChecksAnnotation checksAnnotation) {
        return new Annotation(checksAnnotation.getPath(),
                checksAnnotation.getStartLine(), checksAnnotation.getEndLine(),
                getAnnotationLevel(checksAnnotation.getAnnotationLevel()),
                checksAnnotation.getMessage());
    }

    private Image getImage(final ChecksImage checksImage) {
        return new Image(checksImage.getAlt(), checksImage.getImageUrl()).withCaption(checksImage.getCaption());
    }

    private AnnotationLevel getAnnotationLevel(final ChecksAnnotationLevel checksLevel) {
        // FIXME: should create a lookup table?
        switch (checksLevel) {
            case NOTICE:
                return AnnotationLevel.NOTICE;
            case FAILURE:
                return AnnotationLevel.FAILURE;
            case WARNING:
                return AnnotationLevel.WARNING;
            default:
                return null;
        }
    }
}
