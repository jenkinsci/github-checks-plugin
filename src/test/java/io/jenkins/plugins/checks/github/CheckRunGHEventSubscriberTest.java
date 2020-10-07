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
        SCMFacade scmFacade = mock(SCMFacade.class);
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
    void shouldProcessCheckRunEventWithRerequestedAction() throws IOException {
        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1);
        new CheckRunGHEventSubscriber(mock(JenkinsFacade.class), mock(SCMFacade.class))
                .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
        assertThat(loggerRule.getMessages().get(0)).contains("Received rerun request through GitHub checks API.");
    }

    @Test
    void shouldIgnoreCheckRunEventWithoutRerequestedAction() throws IOException {
        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.FINE).capture(1);
        new CheckRunGHEventSubscriber(mock(JenkinsFacade.class), mock(SCMFacade.class))
                .onEvent(createEventWithRerunRequest("check-run-event-with-created-action.json"));
        assertThat(loggerRule.getMessages()).contains("Unsupported check run action: created");
    }

    @Test
    void shouldScheduleRerunForPR() throws IOException {
        Job<?, ?> job = mock(Job.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        SCMFacade scmFacade = mock(SCMFacade.class);

        when(jenkinsFacade.getJob("XiongKezhi/codingstyle/PR-1")).thenReturn(Optional.of(job));
        when(jenkinsFacade.getFullNameOf(job)).thenReturn("codingstyle/PR-1");
        when(job.getNextBuildNumber()).thenReturn(1);
        when(job.getName()).thenReturn("PR-1");

        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1);
        new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade)
                .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
        assertThat(loggerRule.getMessages())
                .contains("Scheduled rerun (build #1) for job codingstyle/PR-1, requested by XiongKezhi");
    }

    @Test
    void shouldScheduleRerunForMaster() throws IOException {
        Job<?, ?> job = mock(Job.class);
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        SCMFacade scmFacade = mock(SCMFacade.class);

        when(jenkinsFacade.getJob("XiongKezhi/codingstyle/master")).thenReturn(Optional.of(job));
        when(jenkinsFacade.getFullNameOf(job)).thenReturn("codingstyle/master");
        when(job.getNextBuildNumber()).thenReturn(1);
        when(job.getName()).thenReturn("master");

        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.INFO).capture(1);
        new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade)
                .onEvent(createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_MASTER));
        assertThat(loggerRule.getMessages())
                .contains("Scheduled rerun (build #1) for job codingstyle/master, requested by XiongKezhi");
    }

    @Test
    void shouldNotScheduleRerunWhenNoProperJobFound() throws IOException {
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.getAllJobs()).thenReturn(Collections.emptyList());

        assertNoBuildIsScheduled(jenkinsFacade, mock(SCMFacade.class),
                createEventWithRerunRequest(RERUN_REQUEST_JSON_FOR_PR));
    }

    @Test
    void shouldContainsUserInShortDescriptionOfGitHubChecksRerunActionCause() {
        CheckRunGHEventSubscriber.GitHubChecksRerunActionCause cause =
                new CheckRunGHEventSubscriber.GitHubChecksRerunActionCause("jenkins");

        assertThat(cause.getShortDescription()).isEqualTo("Rerun request by jenkins through GitHub checks API");
    }

    private void assertNoBuildIsScheduled(final JenkinsFacade jenkinsFacade, final SCMFacade scmFacade,
                                          final GHSubscriberEvent event) {
        loggerRule.record(CheckRunGHEventSubscriber.class.getName(), Level.WARNING).capture(1);
        new CheckRunGHEventSubscriber(jenkinsFacade, scmFacade).onEvent(event);
        assertThat(loggerRule.getMessages())
                .contains("No job found for rerun request from repository: XiongKezhi/codingstyle and job: XiongKezhi/codingstyle/PR-1");
    }

    private GHSubscriberEvent createEventWithRerunRequest(final String jsonFile) throws IOException {
        return new GHSubscriberEvent("CheckRunGHEventSubscriberTest", GHEvent.CHECK_RUN,
                FileUtils.readFileToString(new File(getClass().getResource(getClass().getSimpleName() + "/"
                                + jsonFile).getFile()), StandardCharsets.UTF_8));
    }
}
