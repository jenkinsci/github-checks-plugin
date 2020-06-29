package io.jenkins.plugins.checks.api;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;

import static io.jenkins.plugins.checks.api.ChecksDetailsAssert.*;
import static io.jenkins.plugins.checks.api.ChecksOutputAssert.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link ChecksDetails}.
 */
class ChecksDetailsTest {
    private static final String CHECK_NAME = "Jenkins";

    @Test
    void shouldBuildCorrectlyWithOnlyRequiredFields() {
        ChecksDetails details = new ChecksDetailsBuilder()
                .withName(CHECK_NAME)
                .withStatus(ChecksStatus.QUEUED)
                .build();

        assertThat(details).hasName(Optional.of(CHECK_NAME))
                .hasStatus(ChecksStatus.QUEUED)
                .hasDetailsURL(Optional.empty())
                .hasConclusion(ChecksConclusion.NONE)
                .hasOutput(Optional.empty())
                .hasNoActions();
    }

    @Test
    void shouldBuildCorrectlyWithAllFields() {
        ChecksOutput output = new ChecksOutputBuilder()
                .withTitle("output")
                .withSummary("success")
                .build();
        List<ChecksAction> actions = Arrays.asList(
                new ChecksAction("action_1", "the first action", "1"),
                new ChecksAction("action_2", "the second action", "2"));

        final String detailsURL = "https://ci.jenkins.io";
        final LocalDateTime startedAt = LocalDateTime.of(2020, 6, 27, 1, 10)
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();
        final LocalDateTime completedAt = LocalDateTime.of(2021, 7, 28, 2, 20)
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();

        ChecksDetails details = new ChecksDetailsBuilder()
                .withName(CHECK_NAME)
                .withStatus(ChecksStatus.COMPLETED)
                .withStartedAt(startedAt)
                .withCompletedAt(completedAt)
                .withDetailsURL(detailsURL)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withOutput(output)
                .withActions(actions)
                .build();

        assertThat(details)
                .hasName(Optional.of(CHECK_NAME))
                .hasStatus(ChecksStatus.COMPLETED)
                .hasStartedAt(Optional.of(startedAt))
                .hasCompletedAt(Optional.of(completedAt))
                .hasDetailsURL(Optional.of(detailsURL));

        assertThat(details.getOutput().isPresent())
                .isTrue();
        assertThat(details.getOutput().get())
                .hasTitle(Optional.of("output"))
                .hasSummary(Optional.of("success"));

        assertThat(details.getActions())
                .usingFieldByFieldElementComparator()
                .containsExactlyElementsOf(actions);
    }
}
