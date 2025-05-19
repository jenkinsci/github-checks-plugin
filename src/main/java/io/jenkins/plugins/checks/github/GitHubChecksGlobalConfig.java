package io.jenkins.plugins.checks.github;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class GitHubChecksGlobalConfig extends GlobalConfiguration {


    private boolean skipProgressUpdates = false;
    private boolean enforceSkipProgressUpdates = false;

    public static GitHubChecksGlobalConfig get() {
        return GlobalConfiguration.all().get(GitHubChecksGlobalConfig.class);
    }

    public GitHubChecksGlobalConfig() {
        load();
    }

    public synchronized boolean isSkipProgressUpdates() {
        return skipProgressUpdates;
    }

    public synchronized void setSkipProgressUpdates(boolean skipProgressUpdates) {
        this.skipProgressUpdates = skipProgressUpdates;
        save();
    }

    public synchronized boolean isEnforceSkipProgressUpdates() {
        return enforceSkipProgressUpdates;
    }

    public synchronized void setEnforceSkipProgressUpdates(boolean enforceSkipProgressUpdates) {
        this.enforceSkipProgressUpdates = enforceSkipProgressUpdates;
        save();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        return true;
    }
}
