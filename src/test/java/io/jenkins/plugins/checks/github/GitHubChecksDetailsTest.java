package io.jenkins.plugins.checks.github;

import org.junit.jupiter.api.Test;

import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;

import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksStatus;

import static org.assertj.core.api.Assertions.*;

class GitHubChecksDetailsTest {
    @Test
    void shouldReturnAllGitHubObjectsCorrectly() {
        ChecksDetails details = new ChecksDetailsBuilder()
                .withName("checks")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withDetailsURL("https://ci.jenkins.io")
                .build();

        GitHubChecksDetails gitHubDetails = new GitHubChecksDetails(details);
        assertThat(gitHubDetails.getName()).isEqualTo("checks");
        assertThat(gitHubDetails.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(gitHubDetails.getConclusion()).isPresent().hasValue(Conclusion.SUCCESS);
        assertThat(gitHubDetails.getDetailsURL()).isPresent().hasValue("https://ci.jenkins.io");
    }
}
