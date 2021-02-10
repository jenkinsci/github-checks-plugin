package io.jenkins.plugins.checks.github;

import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitSCMChecksContextTest {
    @Test
    public void shouldGetRepository() {
        Run run = mock(Run.class);
        GitSCM scm = mock(GitSCM.class);
        UserRemoteConfig config = mock(UserRemoteConfig.class);
        SCMFacade facade = mock(SCMFacade.class);

        when(facade.findGitSCM(run)).thenReturn(Optional.of(scm));
        when(facade.getUserRemoteConfig(scm)).thenReturn(config);

        for (String url : new String[]{
                "git@197.168.2.0:jenkinsci/github-checks-plugin",
                "git@localhost:jenkinsci/github-checks-plugin",
                "git@github.com:jenkinsci/github-checks-plugin",
                "http://github.com/jenkinsci/github-checks-plugin.git",
                "https://github.com/jenkinsci/github-checks-plugin.git"
        }) {
            when(config.getUrl()).thenReturn(url);
            assertThat(new GitSCMChecksContext(run, "", facade).getRepository())
                    .isEqualTo("jenkinsci/github-checks-plugin");
        }
    }
}
