package io.jenkins.plugins.checks.github;

import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.User;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.checks.github.status.GitHubStatusChecksProperties;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.LogRecorder;
import org.kohsuke.github.GHEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

class CheckRunGHEventSubscriberTest {

    private static final String RERUN_REQUEST_JSON_FOR_PR = "check-run-event-with-rerun-action-for-pr.json";
    private static final String RERUN_REQUEST_JSON_FOR_MASTER = "check-run-event-with-rerun-action-for-master.json";
    private static final String RERUN_REQUEST_JSON_FOR_PR_MISSING_CHECKSUITE = "check-run-event-with-rerun-action-for-pr-missing-check-suite.json";
    private static final String RERUN_REQUEST_JSON_FOR_PR_MISSING_CHECKSUITE_HEAD_BRANCH = "check-run-event-with-rerun-action-for-pr-missing-check-suite-head-branch.json";

    @Test
    void shouldBeApplicableForJobWithGitHubSCMSource() {
        Job<?, ?> job = mock(Job.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        GitHubStatusChecksProperties githubStatusChecksProperties = mock(GitHubStatusChecksProperties.class);

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));

        assertThat(new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade, githubStatusChecksProperties).isApplicable(job))
                .isTrue();
    }

    @Test
    void shouldNotBeApplicableForJobWithoutGitHubSCMSource() {
        Job<?, ?> job = mock(Job.class);
        assertThat(new CheckRunGHEventSubscriber().isApplicable(job))
                .isFalse();
    }

    @Test
    void shouldNotBeApplicableForItemThatNotInstanceOfJob() {
        Item item = mock(Item.class);
        assertThat(new CheckRunGHEventSubscriber().isApplicable(item))
                .isFalse();
    }

    @Test
    void shouldSubscribeToCheckRunEvent() {
        assertThat(new CheckRunGHEventSubscriber().events()).containsOnly(GHEvent.CHECK_RUN);
    }

    @Test
    void shouldProcessCheckRunEventWithRerequestedAction() throws IOException {
        try (LogRecorder logRecorder = new LogRecorder().record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1)) {
            new CheckRunGHEventSubscriber(mock(JenkinsFacade.class), mock(SCMFacade.class), mock(GitHubStatusChecksProperties.class))
                    .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
            assertThat(logRecorder.getMessages().get(0)).contains("Received rerun request through GitHub checks API.");
        }
    }

    @Test
    void shouldThrowExceptionWhenCheckSuitesMissingFromPayload() {
        assertThatThrownBy(
                () -> new CheckRunGHEventSubscriber(mock(JenkinsFacade.class), mock(SCMFacade.class), mock(GitHubStatusChecksProperties.class))
                        .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR_MISSING_CHECKSUITE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Could not parse check run event:");
    }

    @Test
    void shouldIgnoreHeadBranchMissingFromPayload() throws IOException {
        try (LogRecorder logRecorder = new LogRecorder().record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1)) {
            new CheckRunGHEventSubscriber(mock(JenkinsFacade.class), mock(SCMFacade.class), mock(GitHubStatusChecksProperties.class))
                    .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR_MISSING_CHECKSUITE_HEAD_BRANCH));
            assertThat(logRecorder.getMessages().get(0)).contains("Received rerun request through GitHub checks API.");
        }
    }

    @Test
    void shouldIgnoreCheckRunEventWithoutRerequestedAction() throws IOException {
        try (LogRecorder logRecorder = new LogRecorder().record(CheckRunGHEventSubscriber.class.getName(), Level.FINE).capture(1)) {
            new CheckRunGHEventSubscriber(mock(JenkinsFacade.class), mock(SCMFacade.class), mock(GitHubStatusChecksProperties.class))
                    .onEvent(createEventWithRerunRequest("check-run-event-with-created-action.json"));
            assertThat(logRecorder.getMessages()).contains("Unsupported check run action: created");
        }
    }

    @Test
    void shouldScheduleRerunForPR() throws IOException {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubStatusChecksProperties githubStatusChecksProperties = mock(GitHubStatusChecksProperties.class);

        when(jenkinsFacade.getBuild("codingstyle/PR-1#2")).thenReturn(Optional.of(run));
        when(jenkinsFacade.getFullNameOf(job)).thenReturn("codingstyle/PR-1");
        when(run.getParent()).thenReturn(job);
        when(run.getAction(ParametersAction.class)).thenReturn(
                new ParametersAction(new StringParameterValue("test_key", "test_value"))
        );
        when(job.getNextBuildNumber()).thenReturn(1);
        when(job.getName()).thenReturn("PR-1");
        when(githubStatusChecksProperties.getRerunActionRole(job)).thenReturn("");
        when(githubStatusChecksProperties.isDisableRerunAction(job)).thenReturn(false);

        try (LogRecorder logRecorder = new LogRecorder().record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1)) {
            new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade, githubStatusChecksProperties)
                    .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
            assertThat(logRecorder.getMessages())
                    .contains("Scheduled rerun (build #1) for job codingstyle/PR-1, requested by XiongKezhi");
        }
    }

    @Test
    void shouldScheduleRerunForMaster() throws IOException {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubStatusChecksProperties githubStatusChecksProperties = mock(GitHubStatusChecksProperties.class);

        when(jenkinsFacade.getBuild("codingstyle/master#8")).thenReturn(Optional.of(run));
        when(jenkinsFacade.getFullNameOf(job)).thenReturn("codingstyle/master");
        when(run.getParent()).thenReturn(job);
        when(run.getAction(ParametersAction.class)).thenReturn(null);
        when(job.getNextBuildNumber()).thenReturn(1);
        when(job.getName()).thenReturn("master");
        when(githubStatusChecksProperties.getRerunActionRole(job)).thenReturn("");
        when(githubStatusChecksProperties.isDisableRerunAction(job)).thenReturn(false);

        try (LogRecorder logRecorder = new LogRecorder().record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1)) {
            new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade, githubStatusChecksProperties)
                    .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_MASTER));
            assertThat(logRecorder.getMessages())
                    .contains("Scheduled rerun (build #1) for job codingstyle/master, requested by XiongKezhi");
        }
    }

    @Test
    void shouldNotScheduleRerunWhenNoProperBuildFound() throws IOException {
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.getBuild("codingstyle/PR-1#2")).thenReturn(Optional.empty());

        assertNoBuildIsScheduled(jenkinsFacade, mock(SCMFacade.class), mock(GitHubStatusChecksProperties.class),
                createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
    }

    @Test
    void shouldNotScheduleRerunWhenDisabled() throws IOException {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubStatusChecksProperties githubStatusChecksProperties = mock(GitHubStatusChecksProperties.class);

        when(jenkinsFacade.getBuild("codingstyle/PR-1#2")).thenReturn(Optional.of(run));
        when(jenkinsFacade.getFullNameOf(job)).thenReturn("codingstyle/PR-1");
        when(run.getParent()).thenReturn(job);
        when(githubStatusChecksProperties.isDisableRerunAction(job)).thenReturn(true);

        try (LogRecorder logRecorder = new LogRecorder().record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1)) {
            new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade, githubStatusChecksProperties)
                    .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
            assertThat(logRecorder.getMessages())
                    .contains("Rerun action is disabled for job codingstyle/PR-1");
        }
    }

    @Test
    void shouldScheduleRerunWhenUserHasRequiredRole() throws IOException {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubStatusChecksProperties githubStatusChecksProperties = mock(GitHubStatusChecksProperties.class);
        User user = mock(User.class);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("test-role");

        when(jenkinsFacade.getBuild("codingstyle/PR-1#2")).thenReturn(Optional.of(run));
        when(jenkinsFacade.getFullNameOf(job)).thenReturn("codingstyle/PR-1");
        when(run.getParent()).thenReturn(job);
        when(run.getAction(ParametersAction.class)).thenReturn(
                new ParametersAction(new StringParameterValue("test_key", "test_value"))
        );
        when(job.getNextBuildNumber()).thenReturn(1);
        when(job.getName()).thenReturn("PR-1");
        when(githubStatusChecksProperties.getRerunActionRole(job)).thenReturn("test-role");
        when(githubStatusChecksProperties.isDisableRerunAction(job)).thenReturn(false);
        try(MockedStatic<User> staticUser = mockStatic(User.class)) {
            staticUser.when(() -> User.get("XiongKezhi")).thenReturn(user);
            when(user.getAuthorities()).thenReturn(userRoles);
            try (LogRecorder logRecorder = new LogRecorder().record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1)) {
                new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade, githubStatusChecksProperties)
                        .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
                assertThat(logRecorder.getMessages())
                        .contains("Scheduled rerun (build #1) for job codingstyle/PR-1, requested by XiongKezhi");
            }
        }
    }

    @Test
    void shouldNotScheduleRerunWhenUserDoesNotHaveRequiredRole() throws IOException {
        Job job = mock(Job.class);
        Run run = mock(Run.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        SCMFacade scmFacade = mock(SCMFacade.class);
        GitHubStatusChecksProperties githubStatusChecksProperties = mock(GitHubStatusChecksProperties.class);
        User user = mock(User.class);
        List<String> userRoles = Collections.<String>emptyList();

        when(jenkinsFacade.getBuild("codingstyle/PR-1#2")).thenReturn(Optional.of(run));
        when(jenkinsFacade.getFullNameOf(job)).thenReturn("codingstyle/PR-1");
        when(run.getParent()).thenReturn(job);
        when(run.getAction(ParametersAction.class)).thenReturn(
                new ParametersAction(new StringParameterValue("test_key", "test_value"))
        );
        when(job.getNextBuildNumber()).thenReturn(1);
        when(job.getName()).thenReturn("PR-1");
        when(githubStatusChecksProperties.getRerunActionRole(job)).thenReturn("test-role");
        when(githubStatusChecksProperties.isDisableRerunAction(job)).thenReturn(false);
        try(MockedStatic<User> staticUser = mockStatic(User.class)) {
            staticUser.when(() -> User.get("XiongKezhi")).thenReturn(user);
            when(user.getAuthorities()).thenReturn(userRoles);
            try (LogRecorder logRecorder = new LogRecorder().record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1)) {
                new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade, githubStatusChecksProperties)
                        .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
                assertThat(logRecorder.getMessages())
                        .contains("The user XiongKezhi does not have the required test-role role for the rerun action on job codingstyle/PR-1");
            }
        }
    }

    @Test
    void shouldContainsUserAndBranchInShortDescriptionOfGitHubChecksRerunActionCause() {
        CheckRunGHEventSubscriber.GitHubChecksRerunActionCause cause =
                new CheckRunGHEventSubscriber.GitHubChecksRerunActionCause("jenkins", "some_branch");

        assertThat(cause.getShortDescription()).isEqualTo("Rerun request by jenkins through GitHub checks API, for branch some_branch");
    }

    @Test
    void shouldHaveAccessibleBranchNameInGitHubChecksRerunActionCause() {
        CheckRunGHEventSubscriber.GitHubChecksRerunActionCause cause =
                new CheckRunGHEventSubscriber.GitHubChecksRerunActionCause("jenkins", "some_branch");

        assertThat(cause.getBranchName()).isEqualTo("some_branch");
    }

    private static void assertNoBuildIsScheduled(final JenkinsFacade jenkinsFacade, final SCMFacade scmFacade,
                                                 final GitHubStatusChecksProperties githubStatusChecksProperties,
                                                 final GHSubscriberEvent event) {
        try (LogRecorder logRecorder = new LogRecorder().record(CheckRunGHEventSubscriber.class.getName(), Level.WARNING).capture(1)) {
            new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade, githubStatusChecksProperties).onEvent(event);
            assertThat(logRecorder.getMessages())
                    .contains("No build found for rerun request from repository: XiongKezhi/codingstyle and id: codingstyle/PR-1#2");
        }
    }

    private static GHSubscriberEvent createEventWithRerunRequest(final String jsonFile) throws IOException {
        return new GHSubscriberEvent("CheckRunGHEventSubscriberTest", GHEvent.CHECK_RUN,
                FileUtils.readFileToString(new File(CheckRunGHEventSubscriberTest.class.getResource(
                        CheckRunGHEventSubscriberTest.class.getSimpleName() + "/" + jsonFile).getFile()), StandardCharsets.UTF_8));
    }
}
