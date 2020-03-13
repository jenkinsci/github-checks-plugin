package io.jenkins.plugins.sources;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.Extension;

import io.jenkins.plugins.extension.CheckRunSource;

/**
 * A {@link CheckRunSource} that only has a default name, it just used as a POC.
 */
@SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
@Extension
public class DefaultCheckRunSource extends CheckRunSource {

    public DefaultCheckRunSource() {
        name = "Default Jenkins Run";
    }

    @Override
    public String getName() {
        return name;
    }
}
