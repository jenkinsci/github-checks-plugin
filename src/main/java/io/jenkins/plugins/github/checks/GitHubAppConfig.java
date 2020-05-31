package io.jenkins.plugins.github.checks;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;

import io.jenkins.plugins.util.GlobalConfigurationFacade;
import io.jenkins.plugins.util.GlobalConfigurationItem;

@Extension
public final class GitHubAppConfig extends GlobalConfigurationItem {

    private static final String DEFAULT_APP_TITLE = "JENKINS";
    private static final String DEFAULT_APP_ID = "0";
    private static final String DEFAULT_KEY = "KEY";

    private String appTitle = DEFAULT_APP_TITLE;
    private String appId = DEFAULT_APP_ID;
    private Secret key;

    public GitHubAppConfig() {
        key = Secret.fromString(DEFAULT_KEY);

        load();
    }

    @VisibleForTesting
    GitHubAppConfig(final GlobalConfigurationFacade facade) {
        super(facade);

        load();
    }

    public static GitHubAppConfig getInstance() {
        return GlobalConfiguration.all().get(GitHubAppConfig.class);
    }

    public String getAppTitle() {
        return appTitle;
    }

    @DataBoundSetter
    public void setAppTitle(@QueryParameter final String appTitle) {
        this.appTitle = appTitle;
        save();
    }

    public String getAppId() {
        return appId;
    }

    @DataBoundSetter
    public void setAppId(@QueryParameter final String appId) {
        this.appId = appId;
        save();
    }

    public Secret getKey() {
        return key;
    }

    @DataBoundSetter
    public void setKey(@QueryParameter final Secret key) {
        this.key = key;
        save();
    }

    public FormValidation doCheckAppTitle(@QueryParameter String appTitle) {
        if (Util.fixEmptyAndTrim(appTitle) == null)
            return FormValidation.error("Invalid title");
        return FormValidation.ok();
    }

    public FormValidation doCheckAppId(@QueryParameter String appId) {
        try {
            Long.parseLong(appId);
        } catch (NumberFormatException e) {
            return FormValidation.error("Invalid id");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckKey(@QueryParameter Secret key) {
        if (Util.fixEmptyAndTrim(key.getPlainText()) == null)
            return FormValidation.error("Invalid key");
        return FormValidation.ok();
    }
}
