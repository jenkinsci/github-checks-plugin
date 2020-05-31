package io.jenkins.plugins.github.checks;

import org.junit.jupiter.api.Test;

import hudson.util.FormValidation.Kind;

import io.jenkins.plugins.github.checks.GitHubAppConfig;
import io.jenkins.plugins.util.GlobalConfigurationFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link GitHubAppConfig}.
 *
 * @author Ullrich Hafner
 */
class GitHubAppConfigTest {
    @Test
    void shouldValidateId() {
        GitHubAppConfig config = new GitHubAppConfig(mock(GlobalConfigurationFacade.class));

        assertThat(config.doCheckAppId("Wrong ID").kind).isEqualTo(Kind.ERROR);
        assertThat(config.doCheckAppId("123456").kind).isEqualTo(Kind.OK);
    }
}
