package io.jenkins.plugins.checks.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;

import static io.jenkins.plugins.checks.api.ChecksOutputAssert.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChecksOutputTest {
    private final static String title = "Coverage Report";
    private final static String summary = "All code have been covered";

    @Test
    void shouldBuildCorrectlyWithOnlyRequiredFields() {
        final ChecksOutput checksOutput = new ChecksOutputBuilder(title, summary).build();

        assertThat(checksOutput).hasTitle(title)
                .hasSummary(summary)
                .hasChecksAnnotations(Collections.emptyList())
                .hasChecksImages(Collections.emptyList());
    }

    @Test
    void shouldBuildCorrectlyWithAllFields() {
        final String text = "#Markdown Supported Text";
        final List<ChecksAnnotation> annotations =
                Arrays.asList(mock(ChecksAnnotation.class), mock(ChecksAnnotation.class), mock(ChecksAnnotation.class));
        final List<ChecksImage> images =
                Arrays.asList(mock(ChecksImage.class), mock(ChecksImage.class));

        final ChecksOutput checksOutput = new ChecksOutputBuilder(title, summary)
                .withText(text)
                .withAnnotations(annotations)
                .withImages(images)
                .build();

        assertThat(checksOutput).hasTitle(title)
                .hasSummary(summary)
                .hasText(text);
        assertThat(checksOutput.getChecksAnnotations()).hasSameSizeAs(annotations);
        assertThat(checksOutput.getChecksImages()).hasSameSizeAs(images);
    }
}
