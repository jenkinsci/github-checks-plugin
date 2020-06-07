package io.jenkins.plugins.github.checks.api;

import java.util.ArrayList;
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
        assertThat(details.getDetailsURL()).isNotPresent();
        assertThat(details.getConclusion()).isNotPresent();
        assertThat(details.getOutputs().isPresent()).isFalse();
        assertThat(details.getActions().isPresent()).isFalse();
    }

    @Test
    void shouldCreateWhenBuildWithAllFields() {
        // TODO: Changes may required here after refactoring Output and Action
        List<Output> outputs = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
        outputs.add(new Output());
        outputs.add(new Output());
        actions.add(new Action());

        final String detailsURL = "ci.jenkins.io";
        ChecksDetailsBuilder builder = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.COMPLETED)
                .withDetailsURL(detailsURL)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withOutputs(outputs)
                .withActions(actions);

        ChecksDetails details = builder.build();
        assertThat(details.getName()).isEqualTo(CHECK_NAME);
        assertThat(details.getStatus()).isEqualTo(ChecksStatus.COMPLETED);
        assertThat(details.getDetailsURL().isPresent()).isTrue();
        assertThat(details.getDetailsURL().get()).isEqualTo(detailsURL);
        assertThat(details.getOutputs().isPresent()).isTrue();
        assertThat(details.getOutputs().get().size()).isEqualTo(2);
        assertThat(details.getActions().isPresent()).isTrue();
        assertThat(details.getActions().get().size()).isEqualTo(1);
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
        assertThatNullPointerException().isThrownBy(() -> builder.withOutputs(null));
        assertThatNullPointerException().isThrownBy(() -> builder.withActions(null));
    }

    @Test
    void shouldThrowsExceptionsWhenBuildWithUnmatchedStatusAndConclusion() {
        ChecksDetailsBuilder builderWithQueued = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.QUEUED);
        ChecksDetailsBuilder builderWithCompleted = new ChecksDetailsBuilder(CHECK_NAME, ChecksStatus.COMPLETED);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> builderWithQueued.withConclusion(ChecksConclusion.SUCCESS));
        assertThatIllegalArgumentException().isThrownBy(() -> builderWithCompleted.build());
    }
}
