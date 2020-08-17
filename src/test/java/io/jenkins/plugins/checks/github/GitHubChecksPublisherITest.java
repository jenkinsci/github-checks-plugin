package io.jenkins.plugins.checks.github;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;

import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.LoggerRule;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.jenkins.plugins.checks.api.ChecksAction;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationBuilder;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksImage;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;
import io.jenkins.plugins.checks.api.ChecksStatus;
import jenkins.scm.api.SCMHead;
import static io.jenkins.plugins.checks.github.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Job;
import hudson.model.Run;
import hudson.util.Secret;

/**
 * Tests if the {@link GitHubChecksPublisher} actually sends out the requests to GitHub in order to publish the check
 * runs.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "checkstyle:ClassDataAbstractionCoupling", "rawtypes"})
public class GitHubChecksPublisherITest {
    /**
     * Rule for Jenkins instance.
     */
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    /**
     * Rule for the log system.
     */
    @Rule
    public LoggerRule loggerRule = new LoggerRule();

    /**
     * A rule which provides a mock server.
     */
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            WireMockConfiguration.options().dynamicPort());

    /**
     * Checks should be published to GitHub correctly when GitHub SCM is found and parameters are correctly set.
     */
    @Test @Ignore("FIXME: wiremock depends on a different Guava version than Jenkins")
    public void shouldPublishGitHubCheckRunCorrectly() {
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
                                        .build()))
                        .withImages(Collections.singletonList(
                                new ChecksImage("Jenkins",
                                        "https://ci.jenkins.io/static/cd5757a8/images/jenkins-header-logo-v2.svg",
                                        "Jenkins Symbol")))
                        .build())
                .withActions(Collections.singletonList(
                        new ChecksAction("re-run", "re-run Jenkins build", "#0")))
                .build();

        new GitHubChecksPublisher(createGitHubChecksContextWithGitHubSCM(), jenkinsRule.createTaskListener(),
                wireMockRule.baseUrl())
                .publish(details);
    }

    /**
     * If exception happens when publishing checks, it should output all parameters of the check to the system log.
     */
    @Issue("issue-20")
    @Test @Ignore("FIXME: wiremock depends on a different Guava version than Jenkins")
    public void shouldLogChecksParametersIfExceptionHappensWhenPublishChecks() {
        loggerRule.record(GitHubChecksPublisher.class.getName(), Level.WARNING).capture(1);

        ChecksDetails details = new ChecksDetailsBuilder()
                .withName("Jenkins")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withOutput(new ChecksOutputBuilder()
                        .withTitle("Jenkins Check")
                        .withSummary("# A Successful Build")
                        .withAnnotations(Collections.singletonList(
                                new ChecksAnnotationBuilder()
                                        .withPath("Jenkinsfile")
                                        .withStartLine(1)
                                        .withEndLine(2)
                                        .withStartColumn(0)
                                        .withEndColumn(20)
                                        .withAnnotationLevel(ChecksAnnotationLevel.WARNING)
                                        .withMessage("say hello to Jenkins")
                                        .build()))
                        .build())
                .build();

        new GitHubChecksPublisher(createGitHubChecksContextWithGitHubSCM(), jenkinsRule.createTaskListener(),
                wireMockRule.baseUrl())
                .publish(details);

        assertThat(loggerRule.getRecords().size()).isEqualTo(1);
        assertThat(loggerRule.getMessages().get(0))
                .contains("Failed Publishing GitHub checks: ")
                .contains("name='Jenkins'")
                .contains("status=COMPLETED")
                .contains("conclusion=SUCCESS")
                .contains("title='Jenkins Check'")
                .contains("summary='# A Successful Build'")
                .contains("path='Jenkinsfile'")
                .contains("startLine=1")
                .contains("endLine=2")
                .contains("startColumn=0")
                .contains("endColumn=20")
                .contains("annotationLevel=WARNING")
                .contains("message='say hello to Jenkins'");
    }

    private GitHubChecksContext createGitHubChecksContextWithGitHubSCM() {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
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
        when(credentials.getPassword()).thenReturn(Secret.fromString("password"));

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(scmFacade.findGitHubAppCredentials(job, "1")).thenReturn(Optional.of(credentials));
        when(scmFacade.findHead(job)).thenReturn(Optional.of(head));
        when(scmFacade.findRevision(source, head)).thenReturn(Optional.of(revision));

        when(urlProvider.getRunURL(run)).thenReturn("https://ci.jenkins.io");

        return new GitHubSCMSourceChecksContext(run, urlProvider.getRunURL(run), scmFacade);
    }
}
