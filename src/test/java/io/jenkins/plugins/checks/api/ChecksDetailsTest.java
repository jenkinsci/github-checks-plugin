package io.jenkins.plugins.checks.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;

import static io.jenkins.plugins.checks.api.ChecksDetailsAssert.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ChecksDetails}.
 */
class ChecksDetailsTest {
    private final String CHECK_NAME = "Jenkins";

    @Test
    void shouldBuildCorrectlyWithOnlyRequiredFields() {
        ChecksDetails details = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED)
                .build();

        assertThat(details).hasName(CHECK_NAME)
                .hasStatus(ChecksStatus.QUEUED)
                .hasDetailsURL(null)
                .hasConclusion(ChecksConclusion.NONE)
                .hasOutput(null)
                .hasActions(Collections.emptyList());
    }

    @Test
    void shouldBuildCorrectlyWithAllFields() {
        ChecksOutput output = mock(ChecksOutput.class);
        List<ChecksAction> actions = Arrays.asList(mock(ChecksAction.class), mock(ChecksAction.class));

        final String detailsURL = "ci.jenkins.io";
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.COMPLETED)
                .withDetailsURL(detailsURL)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withOutput(output)
                .withActions(actions);

        ChecksDetails details = builder.build();
        assertThat(details).hasName(CHECK_NAME)
                .hasStatus(ChecksStatus.COMPLETED)
                .hasDetailsURL(detailsURL)
                .hasOutput(output);
        assertThat(details.getActions()).hasSameSizeAs(actions);
    }

    @Test
    void shouldThrowsExceptionsWhenConstructWithNullParameters() {
        assertThatNullPointerException().isThrownBy(() -> new ChecksDetailsBuilder(CHECK_NAME, null));
        assertThatIllegalArgumentException().
                isThrownBy(() -> new ChecksDetailsBuilder(null, ChecksStatus.QUEUED));
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
