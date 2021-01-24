package io.jenkins.plugins.checks.github;

import java.io.IOException;
import java.util.Collections;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Test;

import io.jenkins.plugins.util.IntegrationTestWithJenkinsPerSuite;
import static org.assertj.core.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;

/**
 * Integration tests for {@link GitSCMChecksContextTest}.
 */
public class GitSCMChecksContextITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String EXISTING_HASH = "4ecc8623b06d99d5f029b66927438554fdd6a467";
    private static final String HTTP_URL = "https://github.com/jenkinsci/github-checks-plugin.git";
    private static final String GIT_URL = "git@github.com:jenkinsci/github-checks-plugin.git";
    private static final String CREDENTIALS_ID = "credentials";
    private static final String URL_NAME = "url";

    /**
     * Creates a FreeStyle job that uses {@link hudson.plugins.git.GitSCM} and runs a successful build.
     * Then this build is used to create a new {@link GitSCMChecksContextTest}. So the build actually is not publishing
     * the checks we just ensure that we can create the context with the successful build (otherwise we would need
     * Wiremock to handle the requests to GitHub).
     */
    @Test
    public void shouldRetrieveContextFromFreeStyleBuild() throws IOException {
        FreeStyleProject job = createFreeStyleProject();
        
        BranchSpec branchSpec = new BranchSpec(EXISTING_HASH);
        GitSCM scm = new GitSCM(GitSCM.createRepoList(HTTP_URL, CREDENTIALS_ID),
                Collections.singletonList(branchSpec), false, Collections.emptyList(), 
                null, null, Collections.emptyList());
        job.setScm(scm);

        Run<?, ?> run = buildSuccessfully(job);

        GitSCMChecksContext gitSCMChecksContext = new GitSCMChecksContext(run, URL_NAME);

        assertThat(gitSCMChecksContext.getRepository()).isEqualTo("jenkinsci/github-checks-plugin");
        assertThat(gitSCMChecksContext.getHeadSha()).isEqualTo(EXISTING_HASH);
        assertThat(gitSCMChecksContext.getCredentialsId()).isEqualTo(CREDENTIALS_ID);
    }

    /**
     * Creates a pipeline that uses {@link hudson.plugins.git.GitSCM} and runs a successful build.
     * Then this build is used to create a new {@link GitSCMChecksContextTest}.
     * The repository url used in this test is in http scheme.
     */
    @Test 
    public void shouldRetrieveContextFromPipelineWithHttpProtocolURL() {
        shouldRetrieveContextFromPipeline(HTTP_URL);
    }

    /**
     * Creates a pipeline that uses {@link hudson.plugins.git.GitSCM} and runs a successful build.
     * Then this build is used to create a new {@link GitSCMChecksContextTest}.
     * The repository url used in this test is in git protocol scheme.
     */
    @Test
    public void shouldRetrieveContextFromPipelineWithGitProtocol() {
        shouldRetrieveContextFromPipeline(GIT_URL);
    }

    private void shouldRetrieveContextFromPipeline(final String url) {
        WorkflowJob job = createPipeline();

        job.setDefinition(new CpsFlowDefinition("node {\n"
                + "  stage ('Checkout') {\n"
                + "    checkout scm: ([\n"
                + "                    $class: 'GitSCM',\n"
                + "                    userRemoteConfigs: [[credentialsId: '" + CREDENTIALS_ID + "', url: '" + url + "']],\n"
                + "                    branches: [[name: '" + EXISTING_HASH + "']]\n"
                + "            ])"
                + "  }\n"
                + "}\n", true));

        Run<?, ?> run = buildSuccessfully(job);

        GitSCMChecksContext gitSCMChecksContext = new GitSCMChecksContext(run, URL_NAME);

        assertThat(gitSCMChecksContext.getRepository()).isEqualTo("jenkinsci/github-checks-plugin");
        assertThat(gitSCMChecksContext.getCredentialsId()).isEqualTo(CREDENTIALS_ID);
        assertThat(gitSCMChecksContext.getHeadSha()).isEqualTo(EXISTING_HASH);
    }
}
