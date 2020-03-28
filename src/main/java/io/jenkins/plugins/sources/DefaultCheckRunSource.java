package io.jenkins.plugins.sources;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.Extension;

import io.jenkins.plugins.extension.CheckRunSource;

/**
 * A {@link CheckRunSource} that only has a default name, it just used as a POC.
 */
@SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
@Extension
public class DefaultCheckRunSource extends CheckRunSource {

    @Override
    public String getName() {
        return "Default Jenkins Run";
    }

    @Override
    public List<Object> getOutput() {
        return null;
    }

    @Override
    public List<Object> getActions() {
        return null;
    }
}
