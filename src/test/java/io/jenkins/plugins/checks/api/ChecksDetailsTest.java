package io.jenkins.plugins.checks.api;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;

import static io.jenkins.plugins.checks.api.ChecksOutputAssert.*;
import static io.jenkins.plugins.checks.api.ChecksDetailsAssert.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link ChecksDetails}.
 */
class ChecksDetailsTest {
    private static final String CHECK_NAME = "Jenkins";

    @Test
    void shouldBuildCorrectlyWithOnlyRequiredFields() {
        ChecksDetails details = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED)
                .build();

        assertThat(details).hasName(CHECK_NAME)
                .hasStatus(ChecksStatus.QUEUED)
                .hasDetailsURL(null)
                .hasConclusion(ChecksConclusion.NONE)
                .hasOutput(null)
                .hasNoActions();
    }

    @Test
    void shouldBuildCorrectlyWithAllFields() {
        ChecksOutput output = new ChecksOutputBuilder("output", "success").build();
        List<ChecksAction> actions = Arrays.asList(
                new ChecksAction("action_1", "the first action", "1"),
                new ChecksAction("action_2", "the second action", "2"));

        final String detailsURL = "https://ci.jenkins.io";
        ChecksDetails details = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.COMPLETED)
                .withDetailsURL(detailsURL)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withOutput(output)
                .withActions(actions)
                .build();

        assertThat(details).hasName(CHECK_NAME)
                .hasStatus(ChecksStatus.COMPLETED)
                .hasDetailsURL(detailsURL);
        assertThat(details.getOutput()).hasTitle("output").hasSummary("success");
        assertThat(details.getActions()).usingFieldByFieldElementComparator().containsExactlyElementsOf(actions);
    }

    @Test
    void shouldThrowExceptionsWhenConstructWithNullParameters() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChecksDetailsBuilder("", ChecksStatus.QUEUED));
        assertThatIllegalArgumentException().
                isThrownBy(() -> new ChecksDetailsBuilder(null, ChecksStatus.QUEUED));
        assertThatNullPointerException()
                .isThrownBy(() -> new ChecksDetailsBuilder(CHECK_NAME, null));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChecksDetailsBuilder(null, null));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ChecksDetailsBuilder("", null));
    }

    @Test
    void shouldThrowExceptionsWhenBuildWithNullParameters() {
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED);

        assertThatNullPointerException().isThrownBy(() -> builder.withDetailsURL(null));
        assertThatIllegalArgumentException().isThrownBy(() -> builder.withConclusion(null));
        assertThatNullPointerException().isThrownBy(() -> builder.withOutput(null));
        assertThatNullPointerException().isThrownBy(() -> builder.withActions(null));
    }

    @Test
    void shouldThrowExceptionsWhenBuildWithUnmatchedStatusAndConclusion() {
        ChecksDetailsBuilder builderWithQueued = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED);
        ChecksDetailsBuilder builderWithCompleted = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.COMPLETED);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> builderWithQueued.withConclusion(ChecksConclusion.SUCCESS));
        assertThatIllegalArgumentException().isThrownBy(builderWithCompleted::build);
    }

    @Test
    void shouldThrowExceptionsWhenBuildWithInvalidDetailsURL() {
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED);

        assertThatIllegalArgumentException().isThrownBy(() -> builder.withDetailsURL("ci.jenkins.io"));
        assertThatIllegalArgumentException().isThrownBy(() -> builder.withDetailsURL("ftp://ci.jenkin.io"));
    }
}
