package io.jenkins.plugins.checks.api;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;

import static io.jenkins.plugins.checks.api.ChecksDetailsAssert.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link ChecksDetails}.
 */
class ChecksDetailsTest {
    private static final String CHECK_NAME = "Jenkins";

    @Test
    void shouldBuildCorrectlyWithAllFields() {
        final ChecksOutput output = new ChecksOutputBuilder()
                .withTitle("output")
                .withSummary("success")
                .build();
        final List<ChecksAction> actions = createActions();
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
                .withActions(actions.subList(0, 1))
                .addAction(actions.get(1))
                .build();

        assertThat(details)
                .hasName(Optional.of(CHECK_NAME))
                .hasStatus(ChecksStatus.COMPLETED)
                .hasStartedAt(Optional.of(startedAt))
                .hasCompletedAt(Optional.of(completedAt))
                .hasDetailsURL(Optional.of(detailsURL))
                .hasConclusion(ChecksConclusion.SUCCESS);

        assertThat(details.getOutput())
                .usingFieldByFieldValueComparator()
                .contains(output);

        assertThat(details.getActions())
                .usingFieldByFieldElementComparator()
                .containsExactlyElementsOf(actions);
    }

    @Test
    void shouldBuildCorrectlyWhenAddingActions() {
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder();
        final List<ChecksAction> actions = Arrays.asList(
                new ChecksAction("action_1", "the first action", "1"),
                new ChecksAction("action_2", "the second action", "2"));
        actions.forEach(builder::addAction);

        assertThat(builder.build().getActions())
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(actions);
    }

    private List<ChecksAction> createActions() {
        final List<ChecksAction> actions = new ArrayList<>();
        actions.add(new ChecksAction("action_1", "the first action", "1"));
        actions.add(new ChecksAction("action_2", "the second action", "2"));
        return actions;
    }
}
