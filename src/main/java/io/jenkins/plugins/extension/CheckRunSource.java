package io.jenkins.plugins.extension;

import java.util.Date;
import java.util.List;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Run;
import jenkins.model.Jenkins;

import io.jenkins.plugins.CheckRunAction;

/**
 * Provides information of a check run; it will be attached to a run through the {@link CheckRunAction}
 * For now we only provide name, more attributes like status, summary, conclusions, etc. can be provided here later, or
 * we can simply use the GHCheckRun class from GitHub API library.These attributes allow other plugins to provide
 * additional build information whiling building in order to to update the GitHub check runs.
 */
public abstract class CheckRunSource extends AbstractDescribableImpl<CheckRunSource> implements ExtensionPoint {

    public abstract String getName();
    public abstract List<Object> getOutput();
    public abstract List<Object> getActions();

    public String getDetailsUrl(Run<?, ?> build) {
        return build.getUrl();
    };

    public String getExternalId(Run<?, ?> build) {
        return build.getId();
    }

    public Date getStartedAt(Run<?, ?> build) {
        return new Date(build.getStartTimeInMillis());
    }

    public Date getCompletedAt(Run<?, ?> build) {
        return new Date(build.getDuration());
    }

    public static ExtensionList<CheckRunSource> all() {
        return Jenkins.get().getExtensionList(CheckRunSource.class);
    }
}
