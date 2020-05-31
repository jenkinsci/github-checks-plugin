package io.jenkins.plugins.github.checks;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
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

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
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

import io.jenkins.plugins.github.checks.api.CheckRunResult;

@Extension
public class JobListener extends RunListener<Run<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(JobListener.class.getName());

    /**
     * API URL for GitHub
     */
    private String apiUrl = "https://api.github.com/";

    /**
     * {@inheritDoc}
     * When a job is initializing, we create check runs implemented by consumers and set to 'pending' state.
     */
    @Override
    public void onInitialize(Run run) {
        LOGGER.log(Level.FINE, "onInitialize");

        // extract GitHub source and revision
        final GitHubSCMSource source = (GitHubSCMSource) SCMSource.SourceByItem.findSource(run.getParent());
        final SCMHead head = SCMHead.HeadByItem.findHead(run.getParent());
        if (head instanceof PullRequestSCMHead) {
            try {
                // get repository and head sha
                String repoFullName = source.getRepoOwner() + "/" + source.getRepository();
                String headSha = resolveHeadCommit(source.fetch(head, null));

                GitHubAppCredentials appCredentials = CredentialsProvider.findCredentialById(
                        StringUtils.defaultIfEmpty(source.getCredentialsId(), ""),
                        GitHubAppCredentials.class, run);
                if (appCredentials != null) {
                    // create token
                    String token = Secret.toString(appCredentials.getPassword());

                    // create check runs based on the information from implementation of sources
                    for (CheckRunResult runSource : CheckRunResult.all()) {
                        long checkRunId = createCheckRun(runSource, repoFullName, headSha, token);
                        run.addAction(new CheckRunResultAction(checkRunId, runSource));
                    }
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.WARNING,
                        "Could not update check runs to PENDING. Message: " + e.getMessage(),
                        LOGGER.isLoggable(Level.FINE) ? e : null);
            }
        }
    }

    /**
     * {@inheritDoc}
     * When a job is starting, we simply set all the related check runs into 'in_progress' state.
     */
    @Override
    public void onStarted(Run run, TaskListener listener) {
        LOGGER.log(Level.FINE, "onStarted");

        // extract GitHub source and revision
        final GitHubSCMSource source = (GitHubSCMSource) SCMSource.SourceByItem.findSource(run.getParent());
        final SCMHead head = SCMHead.HeadByItem.findHead(run.getParent());
        if (head instanceof PullRequestSCMHead) {
            try {
                // get repository full name
                String repoFullName = source.getRepoOwner() + "/" + source.getRepository();

                GitHubAppCredentials appCredentials = CredentialsProvider.findCredentialById(
                        StringUtils.defaultIfEmpty(source.getCredentialsId(), ""),
                        GitHubAppCredentials.class, run);
                if (appCredentials != null) {
                    // create token
                    String token = Secret.toString(appCredentials.getPassword());

                    // create check runs based on the information from implementation of sources
                    for (CheckRunResultAction action : run.getActions(CheckRunResultAction.class))
                        updateCheckRun(action.getCheckRunId(), repoFullName, token);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING,
                        "Could not update check runs to START. Message: " + e.getMessage(),
                        LOGGER.isLoggable(Level.FINE) ? e : null);
            }
        }
    }

    /**
     * {@inheritDoc}
     * When a job is completed, we complete all the related check runs with parameters.
     */
    @Override
    public void onCompleted(Run run, TaskListener listener) {
        LOGGER.log(Level.FINE, "onCompleted");

        // extract GitHub source and revision
        final GitHubSCMSource source = (GitHubSCMSource) SCMSource.SourceByItem.findSource(run.getParent());
        final SCMHead head = SCMHead.HeadByItem.findHead(run.getParent());
        if (head instanceof PullRequestSCMHead) {
            try {
                // get repository and head sha
                // TODO: Use github with app credential because of the repositories maybe private
                GitHub gitHub = new GitHubBuilder().build();
                GHRepository repository = gitHub.getRepository(source.getRepoOwner() + "/" + source.getRepository());

                GitHubAppCredentials appCredentials = CredentialsProvider.findCredentialById(
                        StringUtils.defaultIfEmpty(source.getCredentialsId(), ""),
                        GitHubAppCredentials.class, run);
                if (appCredentials != null) {
                    // create token
                    String token = Secret.toString(appCredentials.getPassword());

                    // create check runs based on the information from implementation of sources
                    for (CheckRunResultAction action : run.getActions(CheckRunResultAction.class))
                        completeCheckRun(action.getCheckRunId(), repository.getFullName(), token);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING,
                        "Could not update check runs to COMPLETED. Message: " + e.getMessage(),
                        LOGGER.isLoggable(Level.FINE) ? e : null);
            }
        }
    }

    /**
     * Simply create a check run in queued state
     *
     * @param source    check run information source
     * @param fullName  repository fullName the check run belongs to
     * @param headSha   commit sha the check run belongs to
     * @param token     api access token for a installation
     * @throws IOException
     *
     * @return Id of the created check run
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
     * @param checkRunId    Check run id
     * @param fullName      Full name for the GitHub repository
     * @param token         Installation token
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
     * @param checkRunId    Check run id
     * @param fullName      Full name for the GitHub repository
     * @param token         Installation token
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

    private static String resolveHeadCommit(SCMRevision revision) throws IllegalArgumentException {
        if (revision instanceof SCMRevisionImpl) {
            return ((SCMRevisionImpl) revision).getHash();
        } else if (revision instanceof PullRequestSCMRevision) {
            return ((PullRequestSCMRevision) revision).getPullHash();
        } else {
            throw new IllegalArgumentException("did not recognize " + revision);
        }
    }
}
