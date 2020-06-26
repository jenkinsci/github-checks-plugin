package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

public class GitHubCheckRunPublishITest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            WireMockConfiguration.options().dynamicPort());

    @Test
    void shouldPublichGitHubCheckRunCorrectly() throws IOException {
        wireMockRule.start();

        Run<?, ?> run = mock(Run.class);
        GitHubChecksContext context = mock(GitHubChecksContext.class);
        when(context.getRepository()).thenReturn("jglick/github-api-test");
        when(context.getHeadSha()).thenReturn("4a929d464a2fae7ee899ce603250f7dab304bc4b");

        ChecksDetails details = new ChecksDetailsBuilder("foo", ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withDetailsURL("http://nowhere.net/stuff")
                .withStartedAt(LocalDateTime.ofEpochSecond(999_999, 0, ZoneOffset.ofHours(8)))
                .withCompletedAt(LocalDateTime.ofEpochSecond(999_999, 999_000_000,
                        ZoneOffset.ofHours(8)))
                .withOutput((new ChecksOutputBuilder("Some Title", "what happened…"))
                        .withAnnotations(Collections.singletonList(
                                new ChecksAnnotationBuilder("stuff.txt", 1, ChecksAnnotationLevel.NOTICE,
                                        "hello to you too")
                                        .withTitle("Look here")
                                        .build()))
                        .withImages(Collections.singletonList(new ChecksImage("Unikitty",
                                "https://i.pinimg.com/474x/9e/65/c0/9e65c0972294f1e10f648c9780a79fab.jpg")
                                .withCaption("Princess Unikitty")))
                        .build())
                .withActions(Collections.singletonList(new ChecksAction("Help", "what I need help with",
                        "doit")))
                .build();

        GitHub gitHub = new GitHubBuilder().withEndpoint(wireMockRule.baseUrl()).build();

        GitHubChecksPublisher publisher = new GitHubChecksPublisher(run);
        publisher.createBuilder(gitHub, new GitHubChecksDetails(details), context)
                .create();
    }
}
