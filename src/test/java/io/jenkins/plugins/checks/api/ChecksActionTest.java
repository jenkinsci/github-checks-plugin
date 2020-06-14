package io.jenkins.plugins.checks.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ChecksActionTest {
    @Test
    void shouldConstructCorrectly() {
        final ChecksAction action =
                new ChecksAction("re-run", "re-run the Jenkins build", "re-run id");
        assertThat(action.getLabel()).isEqualTo("re-run");
        assertThat(action.getDescription()).isEqualTo("re-run the Jenkins build");
        assertThat(action.getIdentifier()).isEqualTo("re-run id");
    }
}
