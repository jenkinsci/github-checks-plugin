package io.jenkins.plugins.extension;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import jenkins.model.Jenkins;

import io.jenkins.plugins.CheckRunAction;

/**
 * Provides information of a check run; it will be attached to a run through the {@link CheckRunAction}
 * For now we only provide name, more attributes like status, summary, conclusions, etc. can be provided here later, or
 * we can simply use the GHCheckRun class from GitHub API library.These attributes allow other plugins to provide
 * additional build information whiling building in order to to update
 * the GitHub check runs.
 */
public abstract class CheckRunSource extends AbstractDescribableImpl<CheckRunSource> implements ExtensionPoint {

    protected String name;

    public abstract String getName();

    public static ExtensionList<CheckRunSource> all() {
        return Jenkins.get().getExtensionList(CheckRunSource.class);
    }
}
