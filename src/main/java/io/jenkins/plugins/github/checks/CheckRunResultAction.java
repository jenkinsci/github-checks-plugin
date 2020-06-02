package io.jenkins.plugins.github.checks;

import hudson.ExtensionPoint;
import hudson.model.Run;
import jenkins.model.RunAction2;

import io.jenkins.plugins.github.checks.api.CheckRunResult;

/**
 * This class is used as a vehicle of {@link CheckRunResult}. It will be attached to the run after check runs are
 * created.
 */
public class CheckRunResultAction implements RunAction2, ExtensionPoint {
    private transient Run<?, ?> owner;

    private long checkRunId;
    private CheckRunResult source;

    public CheckRunResultAction(final long id, final CheckRunResult source) {
        this.checkRunId = id;
        this.source = source;
    }

    public long getCheckRunId() {
        return checkRunId;
    }

    public CheckRunResult getSource() {
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

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
