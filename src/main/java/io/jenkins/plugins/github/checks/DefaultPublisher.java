package io.jenkins.plugins.github.checks;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import hudson.Extension;

import io.jenkins.plugins.github.checks.api.ChecksPublisher;

/**
 * A default publisher that only provides name of a check run.
 */
@Extension
@Restricted(NoExternalUse.class)
public class DefaultPublisher implements ChecksPublisher {
    public String getName() {
        return "Jenkins Build";
    }
}
