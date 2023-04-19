package io.jenkins.plugins.checks.github.status;

import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSourceCriteria;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GitHubSCMSourceStatusChecksTraitTest {
    @Test
    void shouldOnlyApplyTraitConfigurationsToGitHubBranchSourceNotificationsWhenItsNotDisabled() {
        GitHubSCMSourceContext context = new GitHubSCMSourceContext(mock(SCMSourceCriteria.class),
                mock(SCMHeadObserver.class));
        GitHubSCMSourceStatusChecksTrait trait = new GitHubSCMSourceStatusChecksTrait();

        // disable notifications, the trait configuration should be ignored
        context.withNotificationsDisabled(true);
        trait.setSkipNotifications(false);
        trait.decorateContext(context);
        assertThat(context.notificationsDisabled()).isTrue();

        // enable notifications, the trait configuration should be applied
        context.withNotificationsDisabled(false);
        trait.setSkipNotifications(true);
        trait.decorateContext(context);
        assertThat(context.notificationsDisabled()).isTrue();
    }
}
