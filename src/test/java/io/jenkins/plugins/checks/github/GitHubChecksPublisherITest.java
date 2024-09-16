package io.jenkins.plugins.checks.github;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import hudson.model.Action;
import hudson.model.Result;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.LoggerRule;
import org.mockito.MockedStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.kohsuke.github.GHCheckRun;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.util.Secret;
import jenkins.model.ParameterizedJobMixIn;
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
import io.jenkins.plugins.util.PluginLogger;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests if the {@link GitHubChecksPublisher} actually sends out the requests to GitHub in order to publish the check
 * runs.
 */
@RunWith(Parameterized.class)
@SuppressWarnings({"PMD.ExcessiveImports", "checkstyle:ClassDataAbstractionCoupling", "rawtypes", "checkstyle:ClassFanOutComplexity", "checkstyle:JavaNCSS"})
public class GitHubChecksPublisherITest {

    private static final String TEST_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDWV2v0jCfzbyTi\n" +
            "r3mIufQSvXQj02e0Hbia0BOjYluZ2ife/RMs8mrzxAfWUtyrWsi+50OvbxXx+mk1\n" +
            "drn+aR0z0YJ7gqymvn2zWUDv+99eWSb9yeKT3cZU7EpcwtL8APPLzSycoPeylkf8\n" +
            "jtWopdglWO7AXnA+OIiW/luxgxzjUL6lrzye/9l67qQksy6F42+X5jKTZYx2e3vd\n" +
            "I/NZgCGd/2h61RAHJH/2QwujYva2kc5pvm0JmwHKWqEWu+i6lcGXeL/C3zkyh8To\n" +
            "ROFNMz/12+mUbqye1dAg19JcJtmM8ymHsmfFc9CGmXQyuAuhU4zPssA/2i0rPWl+\n" +
            "xthlEA6TAgMBAAECggEASVrf8nCpF5H5IK+HO3jQhD1cawpl2mm1jR4bKnZ1/QCB\n" +
            "Vrpr/pz0Z3q2Z+4x4V8Phu4k5vxwmUDnEsoQO3aD7QEN0/FT3zkgUeoA5GDiACso\n" +
            "wgB+z7Y9s0Cu7nIqvN4ikaQlWXFpdDAkcNX9X1tqztVR2Ho5lcHJVUu129mQYGbY\n" +
            "ivdmSIjLn9oqFhqOpdYtLSoiNtoJmhyFTQj0G+DTumS9G556sBRuZI7qwAKrd6+D\n" +
            "GPvbgVC7mcGogDgUyIAMLj9Q+EfjlX+gfWtqabF9v5Wxp0u1vdC+mdmL/IgbqGTW\n" +
            "DYEQAS1gkkLYXQZXBp7vREU5Oq/W2/okX4FaRNzW2QKBgQDurWDX3Jh+3Q+raQy4\n" +
            "qyN3WZ4XUPzmVoQ11+GY/SkcFXK3r6xD+FZtjUv8yugarnjdYPeG3SUJKhEhVnl8\n" +
            "Ja2CLruZB6sbfsQ2lA9vKR87upWV4DJftuAdFnWVVMD6ti1KCTHIDiUl0YAxWF6A\n" +
            "EGKDrzQIVdTtBn6+Hrhn59AYtQKBgQDl5eKQiyA1wQ/nO4u+VskcrBcaTQJjNysz\n" +
            "mo9k+jpJQqVrJ2kNopbkaZyz74IXI73rQ8CmctAPrSiucj1SeMBWWPWXDC+hxzjV\n" +
            "NURdmEh7D0fpKAknn2WPrIDrqLgsVTiCEX/XicX3eCTuKf+mSUwv//6MFhDIntC4\n" +
            "2PdCtMD/JwKBgBFEH55eCfYbfdezmMT/NGic5g/fvvvWxGe0v1A2+DNc5did78NX\n" +
            "AsGYGCgocZQEjR/OtPlfpB8+mNClldJCU4P4Z3/RizJJAF7GZTtwaR8EB3A5MMu1\n" +
            "yg6woj70S6WXaj1R3vUO+Ob8ed6X+vYeuVG3afc0ZlvjPWX5iPOTVH2FAoGAYqnc\n" +
            "KChtNGSczKITgSaBvRpl99Wg9q+QjN8CN1XkedhuYaRSQ5XJqFFi/R4G+KNQOI2l\n" +
            "Okn/3Rp1YRiKFMDZ2rTnAWIrdwSm8Wmg44IdaSLPu9KAy05vKc/grEKGeBBC5h9Y\n" +
            "fEoWefRH9SZ1HwpJ9jepKLm3jkIKVapXw3sLcPUCgYBdbDosTe2LtF2buaNsBMQw\n" +
            "H3c9fHllrDSy2Twr12ShSc5xMIqTWtiTAvEcMZYP4BX9uSPUWwaB7wMBx2CCMsXR\n" +
            "sZLRujRKV9s5qSmUOXHQWIcmsEvjyxiNtVNhi3rXeMMgYISleUH4ife4evulPHzD\n" +
            "gppAplykAFg49TGEqr7ihQ==\n" +
            "-----END PRIVATE KEY-----";

    /**
     * Provides parameters for tests.
     * @return A list of methods used to create GitHubChecksContexts, with which each test should be run.
     */
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> contextBuilders() {
        return Arrays.asList(new Object[][]{
                {"Freestyle (run)", (Function<GitHubChecksPublisherITest, GitHubChecksContext>) GitHubChecksPublisherITest::createGitHubChecksContextWithGitHubSCMFreestyle, false},
                {"Freestyle (job)", (Function<GitHubChecksPublisherITest, GitHubChecksContext>) GitHubChecksPublisherITest::createGitHubChecksContextWithGitHubSCMFreestyle, true},
                {"Pipeline (run)", (Function<GitHubChecksPublisherITest, GitHubChecksContext>) GitHubChecksPublisherITest::createGitHubChecksContextWithGitHubSCMFromPipeline, false},
                {"Pipeline (job)", (Function<GitHubChecksPublisherITest, GitHubChecksContext>) GitHubChecksPublisherITest::createGitHubChecksContextWithGitHubSCMFromPipeline, true}
        });
    }

    /**
     * Human readable name of the context builder - used only for test name formatting.
     */
    @SuppressWarnings("checkstyle:VisibilityModifier")
    @Parameterized.Parameter(0)
    public String contextBuilderName;

    /**
     * Reference to method used to create GitHubChecksContext with either a pipeline or freestyle job.
     */
    @SuppressWarnings("checkstyle:VisibilityModifier")
    @Parameterized.Parameter(1)
    public Function<GitHubChecksPublisherITest, GitHubChecksContext> contextBuilder;

    /**
     * Create GitHubChecksContext from the job instead of the run.
     */
    @SuppressWarnings("checkstyle:VisibilityModifier")
    @Parameterized.Parameter(2)
    public boolean fromJob;

    /**
     * Rule for the log system.
     */
    @Rule
    public LoggerRule loggerRule = new LoggerRule();

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * A rule which provides a mock server.
     */
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            WireMockConfiguration.options().dynamicPort());

    private MockedStatic<CredentialsMatchers> mockCredentialsMatchers() {
        final var gitHubAppCredentials = new GitHubAppCredentials(CredentialsScope.GLOBAL, "cred-id", null, "app-id", Secret.fromString(TEST_PRIVATE_KEY));

        final var credentialsMatchers = mockStatic(CredentialsMatchers.class);
        credentialsMatchers.when(() -> CredentialsMatchers.firstOrNull(any(), any())).thenReturn(gitHubAppCredentials);
        return credentialsMatchers;
    }

    /**
     * Checks should be published to GitHub correctly when GitHub SCM is found and parameters are correctly set.
     */
    @Test
    public void shouldPublishGitHubCheckRunCorrectly() {
        try (var credentialsMatchers = mockCredentialsMatchers()) {
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

            new GitHubChecksPublisher(contextBuilder.apply(this),
                    new PluginLogger(j.createTaskListener().getLogger(), "GitHub Checks"),
                    wireMockRule.baseUrl())
                    .publish(details);
        }
    }

    /**
     * If exception happens when publishing checks, it should output all parameters of the check to the system log.
     */
    @Issue("issue-20")
    @Test
    public void shouldLogChecksParametersIfExceptionHappensWhenPublishChecks() {
        try (var credentialsMatchers = mockCredentialsMatchers()) {
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

            new GitHubChecksPublisher(contextBuilder.apply(this),
                    new PluginLogger(j.createTaskListener().getLogger(), "GitHub Checks"),
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

        InjectableValues.Std std = new InjectableValues.Std();
        std.addValue("org.kohsuke.github.connector.GitHubConnectorResponse", null);
        std.addValue("org.kohsuke.github.GitHub", null);
        ObjectReader reader = mapper.reader(std).forType(GHCheckRun.class);

        return reader.readValue(String.format("{\"id\": %d}", id));
    }

    /**
     * Test that publishing a second check with the same name will update rather than overwrite the existing check.
     */
    @Test
    @SuppressFBWarnings(value = "RCN", justification = "False positive of SpotBugs")
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

        try (var credentialsMatchers = mockCredentialsMatchers(); var connector = mockStatic(Connector.class)) {
            connector.when(() -> Connector.lookupScanCredentials(any(), any(), any(), any())).thenCallRealMethod();
            connector.when(() -> Connector.connect(anyString(), any())).thenReturn(gitHub);

            GitHubChecksContext context = contextBuilder.apply(this);

            ChecksDetails details1 = new ChecksDetailsBuilder()
                    .withName(checksName1)
                    .withStatus(ChecksStatus.IN_PROGRESS)
                    .build();

            GitHubChecksPublisher publisher = new GitHubChecksPublisher(context,
                    new PluginLogger(j.createTaskListener().getLogger(), "GitHub Checks"),
                    "https://github.example.com/"
            );

            // Check that the owner is passed from context to credentials
            if (context instanceof GitHubSCMSourceChecksContext) {
                var credentials = publisher.getContext().getCredentials();
                if (credentials instanceof GitHubAppCredentials) {
                    var gitHubAppCredentials = (GitHubAppCredentials) credentials;
                    assertThat(gitHubAppCredentials.getOwner()).isEqualTo("XiongKezhi");
                }
            }

            assertThat(context.getId(checksName1)).isNotPresent();
            assertThat(context.getId(checksName2)).isNotPresent();

            publisher.publish(details1);

            verify(createBuilder1, times(1)).create();
            verify(createBuilder2, never()).create();
            verify(updateBuilder1, never()).create();

            if (fromJob) {
                assertThat(context.getId(checksName1)).isNotPresent();
            }
            else {
                assertThat(context.getId(checksName1)).isPresent().get().isEqualTo(checksId1);
            }
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

            if (fromJob) {
                assertThat(context.getId(checksName1)).isNotPresent();
                assertThat(context.getId(checksName1)).isNotPresent();
            }
            else {
                assertThat(context.getId(checksName1)).isPresent().get().isEqualTo(checksId1);
                assertThat(context.getId(checksName2)).isPresent().get().isEqualTo(checksId2);
            }

            ChecksDetails updateDetails1 = new ChecksDetailsBuilder()
                    .withName(checksName1)
                    .withStatus(ChecksStatus.COMPLETED)
                    .withConclusion(ChecksConclusion.FAILURE)
                    .build();

            publisher.publish(updateDetails1);

            verify(createBuilder1, times(fromJob ? 2 : 1)).create();
            verify(createBuilder2, times(1)).create();
            verify(updateBuilder1, times(fromJob ? 0 : 1)).create();

            if (fromJob) {
                assertThat(context.getId(checksName1)).isNotPresent();
                assertThat(context.getId(checksName1)).isNotPresent();
            }
            else {
                assertThat(context.getId(checksName1)).isPresent().get().isEqualTo(checksId1);
                assertThat(context.getId(checksName2)).isPresent().get().isEqualTo(checksId2);
            }
        }
    }

    private GitHubChecksContext createGitHubChecksContextWithGitHubSCMFreestyle() {
        try {
            FreeStyleProject job = j.createFreeStyleProject();
            return createGitHubChecksContextWithGitHubSCM(job);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private GitHubChecksContext createGitHubChecksContextWithGitHubSCMFromPipeline() {
        try {
            WorkflowJob job = j.createProject(WorkflowJob.class);
            job.setDefinition(new CpsFlowDefinition("node {}", true));
            return createGitHubChecksContextWithGitHubSCM(job);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Run<?, ?> buildSuccessfully(ParameterizedJobMixIn.ParameterizedJob<?, ?> job) throws Exception {
        return j.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0, new Action[0]));
    }

    private <R extends Run<J, R> & Queue.Executable, J extends Job<J, R> & ParameterizedJobMixIn.ParameterizedJob<J, R>>
            GitHubChecksContext createGitHubChecksContextWithGitHubSCM(final J job) throws Exception {
        Run run = buildSuccessfully(job);

        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        SCMHead head = mock(SCMHead.class);
        PullRequestSCMRevision revision = mock(PullRequestSCMRevision.class);
        ClassicDisplayURLProvider urlProvider = mock(ClassicDisplayURLProvider.class);

        when(source.getCredentialsId()).thenReturn("1");
        when(source.getRepoOwner()).thenReturn("XiongKezhi");
        when(source.getRepository()).thenReturn("Sandbox");

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(scmFacade.findGitHubAppCredentials(job, "1")).thenCallRealMethod();
        when(scmFacade.findHead(job)).thenReturn(Optional.of(head));
        when(scmFacade.findRevision(source, run)).thenReturn(Optional.of(revision));
        when(scmFacade.findRevision(source, head)).thenReturn(Optional.of(revision));
        when(scmFacade.findHash(revision)).thenReturn(Optional.of("18c8e2fd86e7aa3748e279c14a00dc3f0b963e7f"));

        when(urlProvider.getRunURL(run)).thenReturn("https://ci.jenkins.io");
        when(urlProvider.getJobURL(job)).thenReturn("https://ci.jenkins.io");

        if (fromJob) {
            return GitHubSCMSourceChecksContext.fromJob(job, urlProvider.getJobURL(job), scmFacade);
        }
        return GitHubSCMSourceChecksContext.fromRun(run, urlProvider.getRunURL(run), scmFacade);
    }
}
