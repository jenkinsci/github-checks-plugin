package io.jenkins.plugins.checks.github;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import io.jenkins.plugins.util.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.util.PluginLogger;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.LoggerRule;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import hudson.model.Run;
import hudson.util.Secret;
import jenkins.scm.api.SCMHead;

import io.jenkins.plugins.checks.api.ChecksAction;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationBuilder;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksImage;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;
import io.jenkins.plugins.checks.api.ChecksStatus;
import org.kohsuke.github.GHCheckRun;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static io.jenkins.plugins.checks.github.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests if the {@link GitHubChecksPublisher} actually sends out the requests to GitHub in order to publish the check
 * runs.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "checkstyle:ClassDataAbstractionCoupling", "rawtypes", "checkstyle:ClassFanOutComplexity"})
public class GitHubChecksPublisherITest extends IntegrationTestWithJenkinsPerTest {

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
    @Test
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

        new GitHubChecksPublisher(createGitHubChecksContextWithGitHubSCM(),
                new PluginLogger(getJenkins().createTaskListener().getLogger(), "GitHub Checks"),
                wireMockRule.baseUrl())
                .publish(details);
    }

    /**
     * If exception happens when publishing checks, it should output all parameters of the check to the system log.
     */
    @Issue("issue-20")
    @Test
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

        new GitHubChecksPublisher(createGitHubChecksContextWithGitHubSCM(),
                new PluginLogger(getJenkins().createTaskListener().getLogger(), "GitHub Checks"),
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

    /**
     * We can't mock the id field on {@link org.kohsuke.github.GHObject}s thanks to {@link com.infradna.tool.bridge_method_injector.WithBridgeMethods}.
     * So, create a stub GHCheckRun with the id we want.
     * @param id id of check run to spoof
     * @return Stubbed {@link GHCheckRun} with only the id of {@link GHCheckRun} set
     */
    private GHCheckRun createStubCheckRun(final long id) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(new VisibilityChecker.Std(NONE, NONE, NONE, NONE, ANY));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        ObjectReader reader = mapper.reader(new InjectableValues.Std().addValue("org.kohsuke.github.GitHubResponse$ResponseInfo", null)).forType(GHCheckRun.class);

        return reader.readValue(String.format("{\"id\": %d}", id));
    }

    /**
     * Test that publishing a second check with the same name will update rather than overwrite the existing check.
     */
    @Test
    public void testChecksPublisherUpdatesCorrectly() throws Exception {
        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        when(gitHub.getRepository(anyString())).thenReturn(repository);

        long checksId1 = 1000;
        long checksId2 = 2000;

        String checksName1 = "Test Updating";
        String checksName2 = "Different Tests";

        GHCheckRunBuilder createBuilder1 = mock(GHCheckRunBuilder.class, RETURNS_SELF);
        GHCheckRunBuilder createBuilder2 = mock(GHCheckRunBuilder.class, RETURNS_SELF);
        GHCheckRunBuilder updateBuilder1 = mock(GHCheckRunBuilder.class, RETURNS_SELF);

        GHCheckRun createResult1 = createStubCheckRun(checksId1);
        GHCheckRun createResult2 = createStubCheckRun(checksId2);

        doReturn(createResult1).when(createBuilder1).create();
        doReturn(createResult2).when(createBuilder2).create();
        doReturn(createResult1).when(updateBuilder1).create();

        when(repository.createCheckRun(eq(checksName1), anyString())).thenReturn(createBuilder1);
        when(repository.createCheckRun(eq(checksName2), anyString())).thenReturn(createBuilder2);
        when(repository.updateCheckRun(checksId1)).thenReturn(updateBuilder1);

        try (MockedStatic<Connector> connector = mockStatic(Connector.class)) {
            connector.when(() -> Connector.connect(anyString(), any(GitHubAppCredentials.class))).thenReturn(gitHub);

            GitHubChecksContext context = createGitHubChecksContextWithGitHubSCM();

            ChecksDetails details1 = new ChecksDetailsBuilder()
                    .withName(checksName1)
                    .withStatus(ChecksStatus.IN_PROGRESS)
                    .build();

            GitHubChecksPublisher publisher = new GitHubChecksPublisher(context,
                    new PluginLogger(getJenkins().createTaskListener().getLogger(), "GitHub Checks"),
                    "https://github.example.com/"
            );

            assertThat(context.getId(checksName1)).isNotPresent();
            assertThat(context.getId(checksName2)).isNotPresent();

            publisher.publish(details1);

            verify(createBuilder1, times(1)).create();
            verify(createBuilder2, never()).create();
            verify(updateBuilder1, never()).create();

            assertThat(context.getId(checksName1)).isPresent().get().isEqualTo(checksId1);
            assertThat(context.getId(checksName2)).isNotPresent();

            ChecksDetails details2 = new ChecksDetailsBuilder()
                    .withName(checksName2)
                    .withStatus(ChecksStatus.COMPLETED)
                    .withConclusion(ChecksConclusion.SUCCESS)
                    .build();

            publisher.publish(details2);

            verify(createBuilder1, times(1)).create();
            verify(createBuilder2, times(1)).create();
            verify(updateBuilder1, never()).create();

            assertThat(context.getId(checksName1)).isPresent().get().isEqualTo(checksId1);
            assertThat(context.getId(checksName2)).isPresent().get().isEqualTo(checksId2);

            ChecksDetails updateDetails1 = new ChecksDetailsBuilder()
                    .withName(checksName1)
                    .withStatus(ChecksStatus.COMPLETED)
                    .withConclusion(ChecksConclusion.FAILURE)
                    .build();

            publisher.publish(updateDetails1);

            verify(createBuilder1, times(1)).create();
            verify(createBuilder2, times(1)).create();
            verify(updateBuilder1, times(1)).create();

            assertThat(context.getId(checksName1)).isPresent().get().isEqualTo(checksId1);
            assertThat(context.getId(checksName2)).isPresent().get().isEqualTo(checksId2);

        }
    }

    private GitHubChecksContext createGitHubChecksContextWithGitHubSCM() {
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition("node {}", true));
        Run run = buildSuccessfully(job);

        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubAppCredentials credentials = mock(GitHubAppCredentials.class);
        SCMHead head = mock(SCMHead.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        ClassicDisplayURLProvider urlProvider = mock(ClassicDisplayURLProvider.class);

        when(source.getCredentialsId()).thenReturn("1");
        when(source.getRepoOwner()).thenReturn("XiongKezhi");
        when(source.getRepository()).thenReturn("Sandbox");
        when(credentials.getPassword()).thenReturn(Secret.fromString("password"));

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(scmFacade.findGitHubAppCredentials(job, "1")).thenReturn(Optional.of(credentials));
        when(scmFacade.findHead(job)).thenReturn(Optional.of(head));
        when(scmFacade.findRevision(source, run)).thenReturn(Optional.of(revision));
        when(scmFacade.findHash(revision)).thenReturn(Optional.of("18c8e2fd86e7aa3748e279c14a00dc3f0b963e7f"));

        when(urlProvider.getRunURL(run)).thenReturn("https://ci.jenkins.io");

        return new GitHubSCMSourceChecksContext(run, urlProvider.getRunURL(run), scmFacade);
    }
}
