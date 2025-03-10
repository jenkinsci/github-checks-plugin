package io.jenkins.plugins.checks.github;

import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksStatus;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHCheckRun.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void shouldReturnEmptyWhenDetailsURLIsBlank() {
        GitHubChecksDetails gitHubChecksDetails =
                new GitHubChecksDetails(new ChecksDetailsBuilder().withDetailsURL(StringUtils.EMPTY).build());
        assertThat(gitHubChecksDetails.getDetailsURL()).isEmpty();
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenDetailsURLIsNotHttpOrHttpsScheme() {
        GitHubChecksDetails gitHubChecksDetails =
                new GitHubChecksDetails(new ChecksDetailsBuilder().withDetailsURL("ci.jenkins.io").build());
        assertThatThrownBy(gitHubChecksDetails::getDetailsURL)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The details url is not http or https scheme: ci.jenkins.io");
    }
}
