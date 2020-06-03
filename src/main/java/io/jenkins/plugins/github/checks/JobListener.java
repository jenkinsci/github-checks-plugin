package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.Secret;

import jenkins.plugins.git.AbstractGitSCMSource.SCMRevisionImpl;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

import io.jenkins.plugins.github.checks.api.AnnotationsBuilder;
import io.jenkins.plugins.github.checks.api.CheckRunResult;
import io.jenkins.plugins.github.checks.api.ChecksBuilder;
import io.jenkins.plugins.github.checks.api.ChecksListener;
import io.jenkins.plugins.util.JenkinsFacade;

@Extension
public class JobListener extends RunListener<Run<?, ?>> {
    private static final Logger LOGGER = Logger.getLogger(JobListener.class.getName());

    /**
     * API URL for GitHub
     */
    private String apiUrl = "https://api.github.com/";

    /**
     * {@inheritDoc}
     *
     * When a job is initializing, we create check runs implemented by consumers and set to 'pending' state.
     */
    @Override
    public void onInitialize(Run run) {
        LOGGER.log(Level.FINE, "onInitialize");

        // extract GitHub source and head
        final GitHubSCMSource source = (GitHubSCMSource) SCMSource.SourceByItem.findSource(run.getParent());
        final SCMHead head = SCMHead.HeadByItem.findHead(run.getParent());
        if (source == null || head == null) {
            return; // not supported source and head
        }

        try {
            String repoFullName = source.getRepoOwner() + "/" + source.getRepository();
            String headSha = resolveHeadCommit(source.fetch(head, null));

            GitHubAppCredentials appCredentials = findGitHubAppCredentials(source, run);
            if (appCredentials != null) {
                // create token
                String token = Secret.toString(appCredentials.getPassword());

                for (ChecksListener listener : new JenkinsFacade().getExtensionsFor(ChecksListener.class)) {
                    ChecksBuilder checks = new ChecksBuilder(listener.getName());
                    // Maybe the annotation builder should not be provided by us
                    AnnotationsBuilder annotations = new AnnotationsBuilder();
                    listener.onQueued(run, checks, annotations);
                    ChecksPublisher.createCheckRun(repoFullName, headSha, token, checks);
                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING,
                    "Could not create check runs. Message: " + e.getMessage(),
                    LOGGER.isLoggable(Level.FINE) ? e : null);
        }
    }

    /**
     * {@inheritDoc}
     *
     * When a job is starting, we simply set all the related check runs into 'in_progress' state.
     */
    @Override
    public void onStarted(Run run, TaskListener listener) {
        LOGGER.log(Level.FINE, "onStarted");

        // extract GitHub source and head
        final GitHubSCMSource source = (GitHubSCMSource) SCMSource.SourceByItem.findSource(run.getParent());
        if (source == null) {
            return; // not supported source and head
        }

        try {
            String repoFullName = source.getRepoOwner() + "/" + source.getRepository();

            GitHubAppCredentials appCredentials = findGitHubAppCredentials(source, run);
            if (appCredentials != null) {
                // create token
                String token = Secret.toString(appCredentials.getPassword());

                for (ChecksListener checksListener : new JenkinsFacade().getExtensionsFor(ChecksListener.class)) {
                    ChecksBuilder checks = new ChecksBuilder(checksListener.getName());
                    // Maybe the annotation builder should not be provided by us
                    AnnotationsBuilder annotations = new AnnotationsBuilder();
                    checksListener.onQueued(run, checks, annotations);
                    ChecksPublisher.updateCheckRun(checks, token);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING,
                    "Could not update check runs to START. Message: " + e.getMessage(),
                    LOGGER.isLoggable(Level.FINE) ? e : null);
        }
    }

    /**
     * {@inheritDoc}
     *
     * When a job is completed, we complete all the related check runs with parameters.
     */
    @Override
    public void onCompleted(Run run, TaskListener listener) {
        LOGGER.log(Level.FINE, "onCompleted");

        // extract GitHub source and head
        final GitHubSCMSource source = (GitHubSCMSource) SCMSource.SourceByItem.findSource(run.getParent());
        if (source == null ) {
            return; // not supported source and head
        }

        try {
            GitHub gitHub = new GitHubBuilder().build();
            GHRepository repository = gitHub.getRepository(
                    source.getRepoOwner() + "/" + source.getRepository() );

            GitHubAppCredentials appCredentials = findGitHubAppCredentials(source, run);
            if (appCredentials != null) {
                // create token
                String token = Secret.toString(appCredentials.getPassword());

                for (ChecksListener checksListener : new JenkinsFacade().getExtensionsFor(ChecksListener.class)) {
                    ChecksBuilder checks = new ChecksBuilder(checksListener.getName());
                    // Maybe the annotation builder should not be provided by us
                    AnnotationsBuilder annotations = new AnnotationsBuilder();
                    checksListener.onQueued(run, checks, annotations);
                    ChecksPublisher.completeCheckRun(checks, token);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING,
                    "Could not update check runs to COMPLETED. Message: " + e.getMessage(),
                    LOGGER.isLoggable(Level.FINE) ? e : null);
        }
    }

    /**
     * Simply create a check run in queued state
     *
     * @param source
     *         check run information source
     * @param fullName
     *         repository fullName the check run belongs to
     * @param headSha
     *         commit sha the check run belongs to
     * @param token
     *         api access token for a installation
     *
     * @throws IOException
     *         if connect GitHub Failed
     *
     * @return the check run id of created check run
     */
    private long createCheckRun(CheckRunResult source, String fullName, String headSha, String token)
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
     * Simply update a check run to in_progress state
     *
     * @param checkRunId
     *         Check run id
     * @param fullName
     *         Full name for the GitHub repository
     * @param token
     *         Installation token
     *
     * @throws IOException
     *         if connect GitHub Failed
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
     * Simply complete check runs, set conclusions and summaries
     *
     * @param checkRunId
     *         Check run id
     * @param fullName
     *         Full name for the GitHub repository
     * @param token
     *         Installation token
     *
     * @throws IOException
     *         if connect GitHub Failed
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

    private static String resolveHeadCommit(SCMRevision revision) throws IllegalArgumentException {
        if (revision instanceof SCMRevisionImpl) {
            return ((SCMRevisionImpl) revision).getHash();
        } else if (revision instanceof PullRequestSCMRevision) {
            return ((PullRequestSCMRevision) revision).getPullHash();
        } else {
            throw new IllegalArgumentException("did not recognize " + revision);
        }
    }

    @CheckForNull
    private static GitHubAppCredentials findGitHubAppCredentials(GitHubSCMSource source, Run<?, ?> run) {
        if (source.getCredentialsId() == null) {
            return null;
        }
        return CredentialsProvider.findCredentialById(source.getCredentialsId(), GitHubAppCredentials.class, run);
    }
}
