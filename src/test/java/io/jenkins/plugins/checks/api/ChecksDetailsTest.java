package io.jenkins.plugins.checks.api;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.github.checks.assertions.Assertions;

/**
 * Tests the class {@link ChecksDetails}.
 */
class ChecksDetailsTest {
    private final String CHECK_NAME = "Jenkins";

    @Test
    void shouldCreateWhenBuildOnlyWithConstructor() {
        ChecksDetails details = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED)
                .build();

        Assertions.assertThat(details).hasName(CHECK_NAME).hasStatus(ChecksStatus.QUEUED);
        Assertions.assertThat(details).hasDetailsURL(null);
        Assertions.assertThat(details).hasConclusion(ChecksConclusion.NONE);
        Assertions.assertThat(details).hasOutput(null);
        Assertions.assertThat(details).hasNoActions();
    }

    @Test
    void shouldCreateWhenBuildWithAllFields() {
        // TODO: Changes may required here after refactoring Output and Action
        // ChecksOutputBuilder outputBuilder = new ChecksOutput();
//        List<ChecksAction> actions = Arrays.asList(new ChecksAction(), new ChecksAction());

        final String detailsURL = "ci.jenkins.io";
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.COMPLETED)
                .withDetailsURL(detailsURL)
                .withConclusion(ChecksConclusion.SUCCESS);
                // .withOutput(checksOutput)
                // .withActions(actions);

        ChecksDetails details = builder.build();
        Assertions.assertThat(details).hasName(CHECK_NAME);
        Assertions.assertThat(details).hasStatus(ChecksStatus.COMPLETED);
        Assertions.assertThat(details).hasDetailsURL(detailsURL);
        // assertThat(details).hasOutput(checksOutput);
        // org.assertj.core.api.Assertions.assertThat(details.getActions()).hasSameSizeAs(actions);

        /* TODO: Implement equals() in output and actions, then uncomment the two lines below
         * assertThat(details).hasOutputs(outputs);
         * assertThat(details).hasActions(actions);
         */
    }

    @Test
    void shouldThrowsExceptionsWhenConstructWithNullParameters() {
        org.assertj.core.api.Assertions.assertThatNullPointerException().isThrownBy(() -> new ChecksDetailsBuilder(CHECK_NAME, null));
        org.assertj.core.api.Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChecksDetailsBuilder(null, ChecksStatus.QUEUED));
        org.assertj.core.api.Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChecksDetailsBuilder(null, ChecksStatus.QUEUED));
        org.assertj.core.api.Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChecksDetailsBuilder("", ChecksStatus.QUEUED));
    }

    @Test
    void shouldThrowsExceptionsWhenBuildWithNullParameters() {
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED);

        org.assertj.core.api.Assertions.assertThatNullPointerException().isThrownBy(() -> builder.withDetailsURL(null));
        org.assertj.core.api.Assertions.assertThatNullPointerException().isThrownBy(() -> builder.withConclusion(null));
        org.assertj.core.api.Assertions.assertThatNullPointerException().isThrownBy(() -> builder.withOutput(null));
        org.assertj.core.api.Assertions.assertThatNullPointerException().isThrownBy(() -> builder.withActions(null));
    }

    @Test
    void shouldThrowsExceptionsWhenBuildWithUnmatchedStatusAndConclusion() {
        ChecksDetailsBuilder builderWithQueued = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED);
        ChecksDetailsBuilder builderWithCompleted = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.COMPLETED);

        org.assertj.core.api.Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> builderWithQueued.withConclusion(ChecksConclusion.SUCCESS));
        org.assertj.core.api.Assertions.assertThatIllegalArgumentException().isThrownBy(builderWithCompleted::build);
    }
}
