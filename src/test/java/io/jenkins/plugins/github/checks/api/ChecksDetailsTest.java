package io.jenkins.plugins.github.checks.api;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.github.checks.ChecksConclusion;
import io.jenkins.plugins.github.checks.ChecksStatus;
import io.jenkins.plugins.github.checks.api.ChecksDetails.ChecksDetailsBuilder;

import static io.jenkins.plugins.github.checks.assertions.Assertions.*;

/**
 * Tests the class {@link ChecksDetails}.
 */
class ChecksDetailsTest {
    private final String CHECK_NAME = "Jenkins";

    @Test
    void shouldCreateWhenBuildOnlyWithConstructor() {
        ChecksDetails details = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED)
                .build();

        assertThat(details).hasName(CHECK_NAME).hasStatus(ChecksStatus.QUEUED);
        assertThat(details).hasDetailsURL(null);
        assertThat(details).hasConclusion(ChecksConclusion.NONE);
        assertThat(details).hasOutput(null);
        assertThat(details).hasNoActions();
    }

    @Test
    void shouldCreateWhenBuildWithAllFields() {
        // TODO: Changes may required here after refactoring Output and Action
        Output output = new Output();
        List<Action> actions = Arrays.asList(new Action(), new Action());

        final String detailsURL = "ci.jenkins.io";
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.COMPLETED)
                .withDetailsURL(detailsURL)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withOutput(output)
                .withActions(actions);

        ChecksDetails details = builder.build();
        assertThat(details).hasName(CHECK_NAME);
        assertThat(details).hasStatus(ChecksStatus.COMPLETED);
        assertThat(details).hasDetailsURL(detailsURL);
        assertThat(details).hasOutput(output);
        assertThat(details.getActions()).hasSameSizeAs(actions);

        /* TODO: Implement equals() in output and actions, then uncomment the two lines below
         * assertThat(details).hasOutputs(outputs);
         * assertThat(details).hasActions(actions);
         */
    }

    @Test
    void shouldThrowsExceptionsWhenConstructWithNullParameters() {
        assertThatNullPointerException().isThrownBy(() -> new ChecksDetailsBuilder(CHECK_NAME, null));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChecksDetailsBuilder(null, ChecksStatus.QUEUED));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChecksDetailsBuilder(null, ChecksStatus.QUEUED));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChecksDetailsBuilder("", ChecksStatus.QUEUED));
    }

    @Test
    void shouldThrowsExceptionsWhenBuildWithNullParameters() {
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED);

        assertThatNullPointerException().isThrownBy(() -> builder.withDetailsURL(null));
        assertThatNullPointerException().isThrownBy(() -> builder.withConclusion(null));
        assertThatNullPointerException().isThrownBy(() -> builder.withOutput(null));
        assertThatNullPointerException().isThrownBy(() -> builder.withActions(null));
    }

    @Test
    void shouldThrowsExceptionsWhenBuildWithUnmatchedStatusAndConclusion() {
        ChecksDetailsBuilder builderWithQueued = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED);
        ChecksDetailsBuilder builderWithCompleted = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.COMPLETED);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> builderWithQueued.withConclusion(ChecksConclusion.SUCCESS));
        assertThatIllegalArgumentException().isThrownBy(builderWithCompleted::build);
    }
}
