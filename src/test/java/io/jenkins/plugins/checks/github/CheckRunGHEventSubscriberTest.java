package io.jenkins.plugins.checks.github;

import hudson.model.*;
import io.jenkins.plugins.util.JenkinsFacade;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.LoggerRule;
import org.kohsuke.github.GHEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CheckRunGHEventSubscriberTest {
    static final String RERUN_REQUEST_JSON_FOR_PR = "check-run-event-with-rerun-action-for-pr.json";
    static final String RERUN_REQUEST_JSON_FOR_MASTER = "check-run-event-with-rerun-action-for-master.json";

    /**
     * Rule for the log system.
     */
    @Rule
    public LoggerRule loggerRule = new LoggerRule();

    @Test
    void shouldBeApplicableForJobWithGitHubSCMSource() {
        Job<?, ?> job = mock(Job.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));

        assertThat(new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade).isApplicable(job))
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
    void shouldProcessCheckRunEventWithRerunAction() throws IOException {
        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1);
        new CheckRunGHEventSubscriber(mock(JenkinsFacade.class), mock(GitHubSCMFacade.class))
                .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
        assertThat(loggerRule.getMessages().get(0)).contains("Received rerun request through GitHub checks API.");
    }

    @Test
    void shouldIgnoreCheckRunEventWithoutRequestedAction() {
        GHSubscriberEvent event = mock(GHSubscriberEvent.class);
        when(event.getPayload()).thenReturn("{\"action\":\"created\"}");

        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.FINE).capture(1);
        new CheckRunGHEventSubscriber(mock(JenkinsFacade.class), mock(GitHubSCMFacade.class)).onEvent(event);
        assertThat(loggerRule.getMessages()).contains("Unsupported check run event: {\"action\":\"created\"}");
    }

    @Test
    void shouldIgnoreCheckRunEVentWithOtherAction() {
        GHSubscriberEvent event = mock(GHSubscriberEvent.class);
        when(event.getPayload()).thenReturn("{\"action\":\"requested_action\",\"requested_action\":" +
                "{\"identifier\":\"fix\"}}");

        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.FINE).capture(1);
        new CheckRunGHEventSubscriber(mock(JenkinsFacade.class), mock(GitHubSCMFacade.class)).onEvent(event);
        assertThat(loggerRule.getMessages()).contains("Unsupported check run event: " +
                "{\"action\":\"requested_action\",\"requested_action\":{\"identifier\":\"fix\"}}");
    }

    @Test
    void shouldScheduleRerunForPR() throws IOException {
        Job<?, ?> job = mock(Job.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(jenkinsFacade.getAllJobs()).thenReturn(Collections.singletonList(job));
        when(jenkinsFacade.getFullNameOf(job)).thenReturn("Sandbox/PR-1");
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(source.getRepoOwner()).thenReturn("XiongKezhi");
        when(source.getRepository()).thenReturn("Sandbox");
        when(job.getNextBuildNumber()).thenReturn(1);
        when(job.getName()).thenReturn("PR-1");

        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1);
        new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade)
                .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
        assertThat(loggerRule.getMessages())
                .contains("Scheduled rerun (build #1) for job Sandbox/PR-1, requested by XiongKezhi");
    }

    @Test
    void shouldScheduleRerunForMaster() throws IOException {
        Job<?, ?> job = mock(Job.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);

        when(jenkinsFacade.getAllJobs()).thenReturn(Collections.singletonList(job));
        when(jenkinsFacade.getFullNameOf(job)).thenReturn("Sandbox/master");
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(source.getRepoOwner()).thenReturn("XiongKezhi");
        when(source.getRepository()).thenReturn("Sandbox");
        when(job.getNextBuildNumber()).thenReturn(1);
        when(job.getName()).thenReturn("master");

        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1);
        new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade)
                .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_MASTER));
        assertThat(loggerRule.getMessages())
                .contains("Scheduled rerun (build #1) for job Sandbox/master, requested by XiongKezhi");
    }

    @Test
    void shouldNotScheduleRerunWhenNoProperJobFound() throws IOException {
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.getAllJobs()).thenReturn(Collections.emptyList());

        assertNoBuildIsScheduled(jenkinsFacade, mock(GitHubSCMFacade.class),
                createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
    }

    @Test
    void shouldNotScheduleRerunWhenNoSourceForTheJobFound() throws IOException {
        Job<?, ?> job = mock(Job.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(jenkinsFacade.getAllJobs()).thenReturn(Collections.singletonList(job));
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.empty());

        assertNoBuildIsScheduled(jenkinsFacade, scmFacade, createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
    }

    @Test
    void shouldNotScheduleRerunWhenRepositoryOwnersAreDifferrent() throws IOException {
        Job<?, ?> job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(jenkinsFacade.getAllJobs()).thenReturn(Collections.singletonList(job));
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(source.getRepoOwner()).thenReturn("jenkinsci");

        assertNoBuildIsScheduled(jenkinsFacade, scmFacade, createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
    }

    @Test
    void shouldNotScheduleRerunWhenRepositoryNamesAreDifferrent() throws IOException {
        Job<?, ?> job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(jenkinsFacade.getAllJobs()).thenReturn(Collections.singletonList(job));
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(source.getRepoOwner()).thenReturn("XiongKezhi");
        when(source.getRepository()).thenReturn("github-checks-api");

        assertNoBuildIsScheduled(jenkinsFacade, scmFacade, createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
    }

    @Test
    void shouldNotScheduleRerunWhenBranchNamesAreDifferent() throws IOException {
        Job<?, ?> job = mock(Job.class);
        GitHubSCMSource source = mock(GitHubSCMSource.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        GitHubSCMFacade scmFacade = mock(GitHubSCMFacade.class);

        when(jenkinsFacade.getAllJobs()).thenReturn(Collections.singletonList(job));
        when(scmFacade.findGitHubSCMSource(job)).thenReturn(Optional.of(source));
        when(source.getRepoOwner()).thenReturn("XiongKezhi");
        when(source.getRepository()).thenReturn("Sandbox");
        when(job.getName()).thenReturn("PR-2");

        assertNoBuildIsScheduled(jenkinsFacade, scmFacade, createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
    }

    @Test
    void shouldContainsUserInShortDescriptionOfGitHubChecksRerunActionCause() {
        CheckRunGHEventSubscriber.GitHubChecksRerunActionCause cause =
                new CheckRunGHEventSubscriber.GitHubChecksRerunActionCause("jenkins");

        assertThat(cause.getShortDescription()).isEqualTo("Rerun request by jenkins through GitHub checks API");
    }

    private void assertNoBuildIsScheduled(final JenkinsFacade jenkinsFacade, final GitHubSCMFacade scmFacade,
                                          final GHSubscriberEvent event) {
        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.WARNING).capture(1);
        new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade).onEvent(event);
        assertThat(loggerRule.getMessages())
                .contains("No proper job found for the rerun request from repository: XiongKezhi/Sandbox and "
                        + "branch: PR-1");
    }

    private GHSubscriberEvent createEventWithRerunRequest(final String jsonFile) throws IOException {
        return new GHSubscriberEvent("CheckRunGHEventSubscriberTest", GHEvent.CHECK_RUN,
                FileUtils.readFileToString(new File(getClass().getResource(getClass().getSimpleName() + "/"
                                + jsonFile).getFile()), StandardCharsets.UTF_8));
    }
}