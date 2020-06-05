package io.jenkins.plugins.github.checks;

import hudson.ExtensionPoint;
import hudson.model.Run;

import io.jenkins.plugins.github.checks.api.ChecksDetails;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * A client used to interact with platforms like GitHub, GitLab, BitBucketed, etc.
 */
public abstract class ChecksClient implements ExtensionPoint {
    private static final JenkinsFacade jenkins = new JenkinsFacade();

    /**
     * Returns true if the client is applicable to the <code>context</code>
     *
     * @param context
     *         the context of a check
     * @return
     *         true if the client is applicable to the <code>context</code>
     */
    public abstract boolean isApplicable(ChecksContext context);

    /**
     * Create a new check run.
     *
     * @param context
     *         the context of a check
     * @param details
     *         the detailed parameters of a check
     */
    public abstract void createCheckRun(ChecksContext context, ChecksDetails details);

    /**
     * Update a check run.
     *
     * @param context
     *         the context of a check
     * @param details
     *         the detailed parameters of a check
     */
    public abstract void updateCheckRun(ChecksContext context, ChecksDetails details);

    /**
     * Complete a check run.
     *
     * @param context
     *         the context of a check
     * @param details
     *         the detailed parameters of a check
     */
    public abstract void completeCheckRun(ChecksContext context, ChecksDetails details);

    public static void createCheckRun(Run<?, ?> run, ChecksDetails details) {
        ChecksContext context = new ChecksContext(run);
        for (ChecksClient client: jenkins.getExtensionsFor(ChecksClient.class)) {
            if (client.isApplicable(context)) {
                client.createCheckRun(context, details);
            }
        }
    }

    public static void updateCheckRun(Run<?, ?> run, ChecksDetails details) {
        ChecksContext context = new ChecksContext(run);
        for (ChecksClient client: jenkins.getExtensionsFor(ChecksClient.class)) {
            if (client.isApplicable(context)) {
                client.updateCheckRun(context, details);
            }
        }
    }

    public static void completeCheckRun(Run<?, ?> run, ChecksDetails details) {
        ChecksContext context = new ChecksContext(run);
        for (ChecksClient client: jenkins.getExtensionsFor(ChecksClient.class)) {
            if (client.isApplicable(context)) {
                client.completeCheckRun(context, details);
            }
        }
    }
}
