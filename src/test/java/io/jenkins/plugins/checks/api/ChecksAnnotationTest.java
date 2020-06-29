package io.jenkins.plugins.checks.api;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationBuilder;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;

import static io.jenkins.plugins.checks.api.ChecksAnnotationAssert.*;

class ChecksAnnotationTest {
    private final static String PATH =
            "github-checks-api-plugin/src/main/java/io/jenkins/plugins/checks/CheckGHEventSubscriber.java";
    private final static String MESSAGE = "Avoid unused private fields such as 'LOGGER'";

    @Test
    void shouldBuildCorrectlyWithAllFields() {
        final String title = "UnusedPrivateField";
        final String rawDetails = "<violation beginline=\"20\" endline=\"20\" begincolumn=\"33\" endcolumn=\"38\" " +
                "rule=\"UnusedPrivateField\" ruleset=\"Best Practices\" package=\"io.jenkins.plugins.checks\" " +
                "class=\"CheckGHEventSubscriber\" variable=\"LOGGER\" externalInfoUrl=\""
                + "https://pmd.github.io/pmd-6.22.0/pmd_rules_java_bestpractices.html#unusedprivatefield\" "
                + "priority=\"3\">\n" + "Avoid unused private fields such as 'LOGGER'.\n" + "</violation>";

        final ChecksAnnotation annotation = new ChecksAnnotationBuilder()
                .withPath(PATH)
                .withStartLine(20).withEndLine(20)
                .withAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                .withMessage(MESSAGE)
                .withStartColumn(33).withEndColumn(38)
                .withTitle(title)
                .withRawDetails(rawDetails)
                .build();

        assertThat(annotation)
                .hasPath(Optional.of(PATH))
                .hasStartLine(Optional.of(20)).hasEndLine(Optional.of(20))
                .hasAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                .hasMessage(Optional.of(MESSAGE))
                .hasStartColumn(Optional.of(33)).hasEndColumn(Optional.of(38))
                .hasTitle(Optional.of(title))
                .hasRawDetails(Optional.of(rawDetails));

        // test the constructor which takes only one parameter for annotation line
        final ChecksAnnotation annotationWithSameLine = new ChecksAnnotationBuilder()
                .withPath(PATH)
                .withLine(20)
                .withAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                .withMessage(MESSAGE)
                .withStartColumn(33).withEndColumn(38)
                .withTitle(title)
                .withRawDetails(rawDetails)
                .build();

        assertThat(annotationWithSameLine)
                .hasPath(Optional.of(PATH))
                .hasStartLine(Optional.of(20)).hasEndLine(Optional.of(20))
                .hasAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                .hasMessage(Optional.of(MESSAGE))
                .hasStartColumn(Optional.of(33))
                .hasEndColumn(Optional.of(38))
                .hasTitle(Optional.of(title))
                .hasRawDetails(Optional.of(rawDetails));

        // test copy constructor
        final ChecksAnnotation copied = new ChecksAnnotation(annotation);
        assertThat(copied)
                .hasPath(Optional.of(PATH))
                .hasStartLine(Optional.of(20)).hasEndLine(Optional.of(20))
                .hasAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                .hasMessage(Optional.of(MESSAGE))
                .hasStartColumn(Optional.of(33))
                .hasEndColumn(Optional.of(38))
                .hasTitle(Optional.of(title))
                .hasRawDetails(Optional.of(rawDetails));
    }

    @Test
    void shouldBuildCorrectlyWithOnlyRequiredFields() {
        final ChecksAnnotation annotation = new ChecksAnnotationBuilder()
                .withPath(PATH)
                .withStartLine(20)
                .withEndLine(20)
                .withAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                .withMessage(MESSAGE)
                .build();

        assertThat(annotation)
                .hasStartLine(Optional.of(20)).hasEndLine(Optional.of(20))
                .hasAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                .hasMessage(Optional.of(MESSAGE));
    }
}
