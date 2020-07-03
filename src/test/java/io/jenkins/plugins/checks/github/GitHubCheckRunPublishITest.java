package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import hudson.model.Run;

import io.jenkins.plugins.checks.api.ChecksAction;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationBuilder;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksImage;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;
import io.jenkins.plugins.checks.api.ChecksStatus;

import static org.mockito.Mockito.*;

/**
 * Tests if the {@link GitHubChecksPublisher} actually sends out the requests to GitHub in order to publish the check
 * runs.
 */
public class GitHubCheckRunPublishITest {
    /**
     * A rule which provides a mock server.
     */
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            WireMockConfiguration.options().dynamicPort());

    @Test
    void shouldPublishGitHubCheckRunCorrectly() throws IOException {
        wireMockRule.start();

        Run<?, ?> run = mock(Run.class);

        GitHubChecksContext context = mock(GitHubChecksContext.class);
        when(context.getRepository()).thenReturn("XiongKezhi/Sandbox");
        when(context.getHeadSha()).thenReturn("18c8e2fd86e7aa3748e279c14a00dc3f0b963e7f");

        ChecksDetails details = new ChecksDetailsBuilder()
                .withName("Jenkins")
                .withStatus(ChecksStatus.COMPLETED)
                .withDetailsURL("https://ci.jenkins.io")
                .withStartedAt(LocalDateTime.ofEpochSecond(999_999, 0, ZoneOffset.UTC))
                .withCompletedAt(LocalDateTime.ofEpochSecond(999_999, 0, ZoneOffset.UTC))
                .withConclusion(ChecksConclusion.SUCCESS)
                .withOutput(new ChecksOutputBuilder()
                        .withTitle("Jenkins Check")
                        .withSummary("# A Successful Build")
                        .withText("## 0 Failures")
                        .withAnnotations(Arrays.asList(
                                new ChecksAnnotationBuilder()
                                        .withPath("Jenkinsfile")
                                        .withLine(1)
                                        .withAnnotationLevel(ChecksAnnotationLevel.NOTICE)
                                        .withMessage("say hello to Jenkins")
                                        .withStartColumn(0)
                                        .withEndColumn(20)
                                        .withTitle("Hello Jenkins")
                                        .withRawDetails("a simple echo command")
                                        .build(),
                                new ChecksAnnotationBuilder()
                                        .withPath("Jenkinsfile")
                                        .withLine(2)
                                        .withAnnotationLevel(ChecksAnnotationLevel.WARNING)
                                        .withMessage("say hello to GitHub Checks API")
                                        .withStartColumn(0)
                                        .withEndColumn(30)
                                        .withTitle("Hello GitHub Checks API")
                                        .withRawDetails("a simple echo command")
                                        .build()
                        ))
                        .withImages(Collections.singletonList(
                                new ChecksImage("Jenkins",
                                        "https://ci.jenkins.io/static/cd5757a8/images/jenkins-header-logo-v2.svg",
                                        "Jenkins Symbol")
                        ))
                        .build())
                .withActions(Collections.singletonList(
                        new ChecksAction("re-run", "re-run Jenkins build", "#0")))
                .build();

        GitHub gitHub = new GitHubBuilder().withEndpoint(wireMockRule.baseUrl()).build();

        GitHubChecksPublisher publisher = new GitHubChecksPublisher(run);
        publisher.createBuilder(gitHub, new GitHubChecksDetails(details), context)
                .create();
    }
}
