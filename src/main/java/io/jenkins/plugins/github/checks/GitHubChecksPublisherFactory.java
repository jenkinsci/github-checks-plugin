package io.jenkins.plugins.github.checks;

import java.util.Optional;

import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import hudson.Extension;

import io.jenkins.plugins.github.checks.api.ChecksPublisherFactory;

@Extension
public class GitHubChecksPublisherFactory extends ChecksPublisherFactory {
    @Override
    protected Optional<ChecksPublisher> createPublisher(final ChecksContext context) {
        return context.getSource() instanceof GitHubSCMSource ?
                Optional.of(new GitHubChecksPublisher(context)) : Optional.empty();
    }
}
