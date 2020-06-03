package io.jenkins.plugins.github.checks;

import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import io.jenkins.plugins.github.checks.api.ChecksBuilder;

/*
 * This publisher manages interactions with GitHub, including creating and updating check runs.
 */
public class ChecksPublisher {
    public static void createCheckRun(ChecksBuilder checks, String token) {}
    public static void updateCheckRun(ChecksBuilder checks, String token) {}
    public static void completeCheckRun(ChecksBuilder checks, String token) {}
    // maybe extract some common logic to avoid repeating
}
