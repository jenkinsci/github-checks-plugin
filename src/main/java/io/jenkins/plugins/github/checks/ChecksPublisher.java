package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.kohsuke.github.GHCheckRun;
import org.kohsuke.github.GHCheckRun.Status;
import org.kohsuke.github.GitHubBuilder;

import io.jenkins.plugins.github.checks.api.ChecksBuilder;

/*
 * This publisher manages interactions with GitHub, including creating and updating check runs.
 */
class ChecksPublisher {
    public static void createCheckRun(String repository, String headSha, String token, ChecksBuilder checks)
            throws IOException {
        GHCheckRun checkRun = new GitHubBuilder().withAppInstallationToken(token).build()
                .getRepository(repository).createCheckRun(checks.getName(), headSha)
                .withStartedAt(new Date())
                .withDetailsURL(checks.getDetailsURL())
                .withStatus(Status.QUEUED)
                .create();
    }
    public static void updateCheckRun(ChecksBuilder checks, String token) {}
    public static void completeCheckRun(ChecksBuilder checks, String token) {}
    // maybe extract some common logic to avoid repeating
}
