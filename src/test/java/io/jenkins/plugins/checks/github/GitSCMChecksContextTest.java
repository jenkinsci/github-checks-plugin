package io.jenkins.plugins.checks.github;

import hudson.model.Run;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GitSCMChecksContextTest {
    @Test
    void shouldGetRepository() {
        for (String url : new String[]{
                "git@197.168.2.0:jenkinsci/github-checks-plugin",
                "git@localhost:jenkinsci/github-checks-plugin",
                "git@github.com:jenkinsci/github-checks-plugin",
                "http://github.com/jenkinsci/github-checks-plugin.git",
                "https://github.com/jenkinsci/github-checks-plugin.git"
        }) {
            assertThat(new GitSCMChecksContext(mock(Run.class), "").getRepository(url))
                    .isEqualTo("jenkinsci/github-checks-plugin");
        }
    }
}
