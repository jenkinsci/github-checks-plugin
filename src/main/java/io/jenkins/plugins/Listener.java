package io.jenkins.plugins;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import io.jenkins.plugins.extension.CheckRunSource;
import io.jenkins.plugins.util.GHAuthenticateHelper;

@Extension
public class Listener extends RunListener<Run<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(Listener.class.getName());

    /**
     * API URL for GitHub
     */
    private String apiUrl = "https://api.github.com/";

    /**
     * Retrieve GitHub events from subscriber
     */
    private CheckGHEventSubscriber subscriber = CheckGHEventSubscriber.getInstance();

    /**
     * Authenticated GitHub used to retrieve API token for each installation
     */
    private GitHubAppConfig config = GitHubAppConfig.getInstance();

    /**
     * When initializing a Jenkins run, process a related check suite event if exists.
     * This may create multiple check runs, and these check runs will be set in queued state.
     *
     * @param run The run on initializing
     */
    @Override
    public void onInitialize(Run run) {
        LOGGER.log(Level.FINE, "onInitialize");

        // If there are check run actions attached, then we need to creat check runs
        List<GHSubscriberEvent> events = subscriber.getCheckSuiteEvents();
        try { // Find the first match check suite event
            for (GHSubscriberEvent event : subscriber.getCheckSuiteEvents()) {
                JsonNode payload = new ObjectMapper().readTree(event.getPayload());

                // For now, we only support requested action
                String action = payload.get("action").asText();
                if (!action.equals("requested")) {
                    continue;
                }

                String repoUrl = payload.get("repository").get("html_url").asText();
                // Check whether this check suite is the one we want
                if (repoUrl.equals(run.getEnvironment().get("GIT_URL"))) {
                    // The Jenkins run who wants to make check runs should attach actions to the run
                    // run.getActions();

                    // For now, we only use fake check run
                    String fullName = payload.get("repository").get("full_name").asText();
                    String headSha = payload.get("check_suite").get("head_sha").asText();

                    // Create token
                    String installaionId = payload.get("installation").get("id").asText();
                    String token = GHAuthenticateHelper.getInstallationToken(config.getAppId(),
                            installaionId, config.getKey());

                    // Create check runs based on the information of source
                    for(CheckRunSource source : CheckRunSource.all()) {
                        long checkRunId = createCheckRun(source, fullName, headSha, token);

                        // Add check run information as action.
                        // The information should get from extensions of CheckRunSource
                        // For now, we just use a simple CheckRunSource
                        run.addAction(new CheckRunAction(checkRunId, source));
                    }
                    // Remove this event
                    events.remove(event);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            events.clear();
        }
    }

    /**
     * When a run is about to start, we set all the related check runs into in_progress state
     *
     * @param run       The run ready to start
     * @param listener  Listener for the run
     */
    @Override
    public void onStarted(Run run, TaskListener listener) {
        LOGGER.log(Level.FINE, "onStarted");

        // First, find whether there are check run actions attached
        List<CheckRunAction> actions = run.getActions(CheckRunAction.class);
        if (actions.size() == 0)
            return;

        List<GHSubscriberEvent> events = subscriber.getCheckRunEvents();
        try {
            for (GHSubscriberEvent event : events) {
                JsonNode payload = new ObjectMapper().readTree(event.getPayload());

                // For now, we only support created action
                String action = payload.get("action").asText();
                if (!action.equals("created")) {
                    continue;
                }

                String repoUrl = payload.get("repository").get("html_url").asText();
                if (repoUrl.equals(run.getEnvironment(listener).get("GIT_URL"))) {
                    // Create token and run
                    long checkRunId = payload.get("check_run").get("id").asLong();

                    // Create token
                    String installaionId = payload.get("installation").get("id").asText();
                    String token = GHAuthenticateHelper.getInstallationToken(config.getAppId(),
                            installaionId, config.getKey());

                    String fullName = payload.get("repository").get("full_name").asText();
                    for (CheckRunAction runAction : actions) {
                        if (runAction.getCheckRunId() == checkRunId)
                            updateCheckRun(checkRunId, fullName, token);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            events.clear();
        }
    }

    /**
     * When a run is completed, we make conclusions and summaries for check runs
     *
     * @param run
     * @param listener
     */
    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        LOGGER.log(Level.FINE, "onCompleted");

        // First, find whether there are check run actions attached
        List<CheckRunAction> actions = run.getActions(CheckRunAction.class);
        if (actions.size() == 0)
            return;

        Set<GHSubscriberEvent> usedEvents = new HashSet<>();
        List<GHSubscriberEvent> events = subscriber.getCheckRunEvents();
        try {
            for (GHSubscriberEvent event : events) {
                JsonNode payload = new ObjectMapper().readTree(event.getPayload());

                // For now, we only support created action
                String action = payload.get("action").asText();
                if (!action.equals("created")) {
                    continue;
                }

                String repoUrl = payload.get("repository").get("html_url").asText();
                if (repoUrl.equals(run.getEnvironment(listener).get("GIT_URL"))) {
                    // Create token and run
                    long checkRunId = payload.get("check_run").get("id").asLong();

                    // Create token
                    String installationId = payload.get("installation").get("id").asText();
                    String token = GHAuthenticateHelper.getInstallationToken(config.getAppId(),
                            installationId, config.getKey());

                    String fullName = payload.get("repository").get("full_name").asText();
                    for (CheckRunAction runAction : actions) {
                        if (runAction.getCheckRunId() == checkRunId)
                            completeCheckRun(checkRunId, fullName, token);
                    }

                    // Mark this event as used, in order to delete it later
                    usedEvents.add(event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            events.clear();
        }

        events.removeAll(usedEvents);
    }

    /**
     * Create a check run in queued state
     *
     * @param source    check run information source
     * @param fullName  repository fullName the check run belongs to
     * @param headSha   commit sha the check run belongs to
     * @param token     api access token for a installation
     * @throws IOException
     *
     * @return Id of the created check run
     */
    private long createCheckRun(CheckRunSource source, String fullName, String headSha, String token)
            throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost( apiUrl + "/repos/" + fullName + "/check-runs");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/vnd.github.antiope-preview+json");
        httpPost.setHeader("Authorization", "token " + token);

        // create request body
        String json = "{" +
                "\"name\":\"" + source.getName() + "\"," +
                "\"head_sha\":\"" + headSha +"\"," +
                "\"status\":\"" + "queued" +"\"" +
                "}";
        httpPost.setEntity(new StringEntity(json));

        CloseableHttpResponse response = client.execute(httpPost);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            LOGGER.log(Level.WARNING, response.getStatusLine().getReasonPhrase());
            return -1;
        }

        JsonNode entity  = new ObjectMapper().readTree(response.getEntity().getContent());
        return entity.get("id").asLong();
    }

    /**
     * Update a check run to in_progress state
     *
     * @param checkRunId    Check run id
     * @param fullName      Full name for the GitHub repository
     * @param token         Installatoion token
     */
    private void updateCheckRun(long checkRunId, String fullName, String token) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(apiUrl + "/repos/" + fullName + "/check-runs/" + checkRunId);

        httpPatch.setHeader("Content-Type", "application/json");
        httpPatch.setHeader("Accept", "application/vnd.github.antiope-preview+json");
        httpPatch.setHeader("Authorization", "token " + token);

        // current ISO time
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(timeZone);
        String json = "{\"status\":\"in_progress\","
                + "\"started_at\":\"" + dateFormat.format(new Date()) + "\""
                + "}";
        httpPatch.setEntity(new StringEntity(json));

        CloseableHttpResponse response = client.execute(httpPatch);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            LOGGER.log(Level.WARNING, response.getStatusLine().getReasonPhrase());
        }
    }

    /**
     * Complete check runs, set conclusions and summaries
     *
     * @param checkRunId    Check run id
     * @param fullName      Full name for the GitHub repository
     * @param token         Installatoion token
     * @throws IOException
     */
    private void completeCheckRun(long checkRunId, String fullName, String token) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(apiUrl + "/repos/" + fullName + "/check-runs/" + checkRunId);

        httpPatch.setHeader("Content-Type", "application/json");
        httpPatch.setHeader("Accept", "application/vnd.github.antiope-preview+json");
        httpPatch.setHeader("Authorization", "token " + token);

        // current ISO time
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(timeZone);
        String json = "{\"status\":\"completed\","
                + "\"completed_at\":\"" + dateFormat.format(new Date()) + "\","
                + "\"conclusion\":\"success\""
                + "}";
        httpPatch.setEntity(new StringEntity(json));

        CloseableHttpResponse response = client.execute(httpPatch);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            LOGGER.log(Level.WARNING, response.getStatusLine().getReasonPhrase());
        }
    }
}
