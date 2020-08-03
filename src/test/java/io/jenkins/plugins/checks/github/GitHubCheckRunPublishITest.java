package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import hudson.model.Job;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import org.junit.Rule;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

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

        ChecksDetails expectedDetails = new ChecksDetailsBuilder()
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

        Job job = mock(Job.class);
        Run run = mock(Run.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);
        SCMHead head = mock(SCMHead.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        ClassicDisplayURLProvider urlProvider = mock(ClassicDisplayURLProvider.class);

        when(run.getParent()).thenReturn(job);
        when(source.getCredentialsId()).thenReturn("1");
        when(source.getRepoOwner()).thenReturn("XiongKezhi");
        when(source.getRepository()).thenReturn("Sandbox");
        when(revision.getPullHash()).thenReturn("18c8e2fd86e7aa3748e279c14a00dc3f0b963e7f");

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(scmFacade.findGitHubAppCredentials(job, "1")).thenReturn(Optional.of(credentials));
        when(scmFacade.findHead(job)).thenReturn(Optional.of(head));
        when(scmFacade.findRevision(source, head)).thenReturn(Optional.of(revision));

        when(urlProvider.getRunURL(run)).thenReturn("https://ci.jenkins.io");

        new GitHubChecksPublisher(new GitHubChecksContext(job, run, scmFacade, urlProvider))
                .createBuilder(new GitHubBuilder().withEndpoint(wireMockRule.baseUrl()).build(),
                        new GitHubChecksDetails(expectedDetails))
                .create();
    }
}
