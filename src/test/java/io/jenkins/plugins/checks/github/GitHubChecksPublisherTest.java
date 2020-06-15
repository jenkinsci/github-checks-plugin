package io.jenkins.plugins.checks.github;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;

import org.kohsuke.github.GHCheckRun;
import org.kohsuke.github.GHCheckRunBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import io.jenkins.plugins.checks.ChecksContext;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksStatus;

import static org.mockito.Mockito.*;

/**
 * This test class hasn't been determined yet, since we can test {@link GitHubChecksPublisher#createBuilder} in unit
 * test or integration test, but I meet difficulties in any way:
 *
 * 1. unit test: we just need to mock the parameters of the method and verify the returned {@link GHCheckRunBuilder}.
 * However, I haven't found a way to successfully verify the fields of it since its an external class from github-api.
 *
 * 2. integration test: we need to construct the parameters very carefully, since they have to be the parameters
 * that will appear in real practises. By this way, we just need to verify the returned values of the method which is a
 * {@link GHCheckRun} instance.
 */
@Disabled
class GitHubChecksPublisherTest {
    private static final String HEAD_SHA =
            "https://github.com/XiongKezhi/checks-api-plugin/commit/bab8fe98d3e32718b5d44b5ffc1f015ab4f5b7e4";
    private static final String REPO_NAME = "XiongKezhi/checks-api-plugin";
    private static final String URL = "ci.jenkins.io";
    private static final String CHECK_NAME = "JENKINS";

    private ChecksContext createChecksContext() throws IOException, InterruptedException {
        ChecksContext context = mock(ChecksContext.class);

        when(context.getRepository()).thenReturn(REPO_NAME);
        when(context.getHeadSha()).thenReturn(HEAD_SHA);
        when(context.getURL()).thenReturn(URL);

        return context;
    }

    private GitHub createGitHub() throws IOException {
        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);

        when(gitHub.getRepository(REPO_NAME)).thenReturn(repository);

        return gitHub;
    }

    void shouldReturnCorrectBuilderWhenInvokeCreateBuilder() throws IOException, InterruptedException {
        ChecksContext context = createChecksContext();
        GitHub gitHub = createGitHub();

        ChecksDetails details = mock(ChecksDetails.class);
        when(details.getName()).thenReturn(CHECK_NAME);
        when(details.getStatus()).thenReturn(ChecksStatus.QUEUED);
        when(details.getDetailsURL()).thenReturn(null);
        when(details.getConclusion()).thenReturn(ChecksConclusion.NONE);

        GitHubChecksPublisher publisher = new GitHubChecksPublisher(context);
        GHCheckRunBuilder builder = publisher.createBuilder(gitHub, new GitHubChecksDetails(details), context);

        // TODO: verify the fields of the builder
    }
}
