package io.jenkins.plugins;

import javax.annotation.CheckForNull;

import hudson.ExtensionPoint;
import hudson.model.Run;
import jenkins.model.RunAction2;

import io.jenkins.plugins.extension.CheckRunSource;

/**
 * This class is used as a vehicle of {@link CheckRunSource}. It will be attached to the run after check runs are
 * created.
 */
public class CheckRunAction implements RunAction2, ExtensionPoint {

    private transient Run<?, ?> owner;

    private long checkRunId;
    private CheckRunSource source;

    public CheckRunAction(final long id, final CheckRunSource source) {
        this.checkRunId = id;
        this.source = source;
    }

    public long getCheckRunId() {
        return checkRunId;
    }

    public CheckRunSource getSource() {
        return source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttached(final Run<?, ?> run) {
        owner = run;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoad(final Run<?, ?> run) {
        owner = run;
    }

    /**
     * Returns owner of this action
     *
     * @return Owner {@link Run} of this action
     */
    public Run<?, ?> getOwner() {
        return owner;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }
}
