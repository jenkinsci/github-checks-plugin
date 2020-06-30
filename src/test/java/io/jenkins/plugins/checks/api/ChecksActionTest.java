package io.jenkins.plugins.checks.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ChecksActionTest {
    @Test
    void shouldConstructCorrectly() {
        final ChecksAction action =
                new ChecksAction("re-run", "re-run the Jenkins build", "re-run id");

        assertThat(action.getLabel()).isPresent().hasValue("re-run");
        assertThat(action.getDescription()).isPresent().hasValue("re-run the Jenkins build");
        assertThat(action.getIdentifier()).isPresent().hasValue("re-run id");
    }
}
