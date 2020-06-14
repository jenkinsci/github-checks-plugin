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
    void ShouldReturnAllGitHubObjectsCorrectly() {
        ChecksDetails details = new ChecksDetailsBuilder("checks", ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withDetailsURL("ci.jenkins.io")
                .build();

        GitHubChecksDetails gitHubDetails = new GitHubChecksDetails(details);
        assertThat(gitHubDetails.getName()).isEqualTo("checks");
        assertThat(gitHubDetails.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(gitHubDetails.getConclusion()).isEqualTo(Conclusion.SUCCESS);
        assertThat(gitHubDetails.getDetailsURL()).isEqualTo("ci.jenkins.io");
    }
}