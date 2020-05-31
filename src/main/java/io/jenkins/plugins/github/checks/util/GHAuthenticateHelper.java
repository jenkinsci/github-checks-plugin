package io.jenkins.plugins.github.checks.util;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GHAppInstallationToken;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

public class GHAuthenticateHelper {
    /**
     * Returns the GitHub with jwt
     *
     * @param id    GitHub app id
     * @param key   GitHub app private key
     *
     * @return      GitHub with jwt
     * @throws IOException if build GitHub failed
     */
    public static GitHub getGitHub(final String id, final String key) throws IOException {
        String jwtToken = JwtHelper.createJWT(id, key);
        return new GitHubBuilder().withJwtToken(jwtToken).build();
    }

    /**
     * Returns the installation token
     * This method is likely to failed for some unknown reasons
     *
     * @param gitHub GitHub with jwt
     * @param id Installation id
     *
     * @return GitHub app installation Token
     * @throws IOException if create token failed
     */
    public static GHAppInstallationToken getInstallation(final GitHub gitHub, final long id) throws IOException {
        GHAppInstallation appInstallation = gitHub.getApp().getInstallationById(id);
        return appInstallation.createToken().create();
    }

    /**
     * Returns the installtion token
     *
     * @param appId GitHub app id
     * @param installationId GitHub installation id
     * @param key GitHub app private key
     *
     * @return GitHub app installation token
     * @throws IOException if execute http post failed
     */
    public static String getInstallationToken(final String appId, final String installationId, final String key)
            throws IOException {

        String jwt = JwtHelper.createJWT(appId, key);
        HttpPost httpPost = new HttpPost(String.format("https://api.github.com/app/installations/%s/access_tokens",
                installationId));
        httpPost.setHeader("Accept", "application/vnd.github.machine-man-preview+json");
        httpPost.setHeader("Authorization", "Bearer " + jwt);

        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(httpPost);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED)
            return "";

        JsonNode entity = new ObjectMapper().readTree(response.getEntity().getContent());
        return entity.get("token").asText();
    }
}
