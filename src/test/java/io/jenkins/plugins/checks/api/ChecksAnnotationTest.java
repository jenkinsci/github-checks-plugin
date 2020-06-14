package io.jenkins.plugins.checks.api;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationBuilder;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;

import static io.jenkins.plugins.checks.api.ChecksAnnotationAssert.*;
import static org.assertj.core.api.Assertions.*;

class ChecksAnnotationTest {
    private final static String PATH =
            "github-checks-api-pluginsrc\\main\\java\\io\\jenkins\\plugins\\checks\\CheckGHEventSubscriber.java";
    private final static String MESSAGE = "Avoid unused private fields such as 'LOGGER'";

    @Test
    void shouldBuildCorrectlyWithAllFields() {
        final String title = "UnusedPrivateField";
        final String rawDetails = "<violation beginline=\"20\" endline=\"20\" begincolumn=\"33\" endcolumn=\"38\" " +
                "rule=\"UnusedPrivateField\" ruleset=\"Best Practices\" package=\"io.jenkins.plugins.checks\" " +
                "class=\"CheckGHEventSubscriber\" variable=\"LOGGER\" externalInfoUrl=\""
                + "https://pmd.github.io/pmd-6.22.0/pmd_rules_java_bestpractices.html#unusedprivatefield\" "
                + "priority=\"3\">\n" + "Avoid unused private fields such as 'LOGGER'.\n" + "</violation>";

        final ChecksAnnotation annotation = new ChecksAnnotationBuilder(PATH, 20, 20,
                ChecksAnnotationLevel.NOTICE, MESSAGE)
                .withStartColumn(33).withEndColumn(38)
                .withTitle(title)
                .withRawDetails(rawDetails)
                .build();

        assertThat(annotation).hasPath(PATH)
                .hasStartLine(20)
                .hasEndLine(20)
                .hasAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                .hasMessage(MESSAGE)
                .hasStartColumn(33)
                .hasEndColumn(38)
                .hasTitle(title)
                .hasRawDetails(rawDetails);
    }

    @Test
    void shouldBuildCorrectlyWithOnlyRequiredFields() {
        final ChecksAnnotation annotation = new ChecksAnnotationBuilder(PATH, 20, 20,
                ChecksAnnotationLevel.NOTICE, MESSAGE)
                .build();

        assertThat(annotation).hasStartLine(20)
                .hasEndLine(20)
                .hasAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                .hasMessage(MESSAGE);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenBuildWithColumnsAndDifferentLineAttributes() {
        final ChecksAnnotationBuilder builder = new ChecksAnnotationBuilder(PATH, 20, 21,
                ChecksAnnotationLevel.WARNING, MESSAGE);

        assertThatIllegalArgumentException().isThrownBy(() -> builder.withStartColumn(0));
        assertThatIllegalArgumentException().isThrownBy(() -> builder.withEndColumn(1));
    }
}
