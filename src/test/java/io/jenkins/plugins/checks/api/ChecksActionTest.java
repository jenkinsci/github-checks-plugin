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

        // test copy constructor
        final ChecksAction copied = new ChecksAction(action);
        assertThat(copied.getLabel()).isEqualTo("re-run");
        assertThat(copied.getDescription()).isEqualTo("re-run the Jenkins build");
        assertThat(copied.getIdentifier()).isEqualTo("re-run id");
    }
}
