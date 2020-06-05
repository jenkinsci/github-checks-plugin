package io.jenkins.plugins.github.checks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
        return "Jenkins";
    }

    @Override
    public Set<ChecksStatus> autoStatus() {
        return new HashSet<>(Arrays.asList(
                ChecksStatus.Queued, ChecksStatus.InProgress, ChecksStatus.Completed));
    }
}
