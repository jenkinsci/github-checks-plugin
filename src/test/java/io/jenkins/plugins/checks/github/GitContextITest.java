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
 * Integration tests for {@link GitSCMChecksContext}.
 */
public class GitContextITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String EXISTING_HASH = "4ecc8623b06d99d5f029b66927438554fdd6a467";
    private static final String HTTP_URL = "https://github.com/jenkinsci/github-checks-plugin.git";
    private static final String CREDENTIALS_ID = "credentials";
    private static final String URL_NAME = "url";
    
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

    @Test 
    public void shouldRetrieveContextFromPipeline() {
        WorkflowJob job = createPipeline();
        
        job.setDefinition(new CpsFlowDefinition("node {\n" 
                + "  stage ('Checkout') {\n" 
                + "    checkout scm: ([\n"
                + "                    $class: 'GitSCM',\n"
                + "                    userRemoteConfigs: [[credentialsId: '" + CREDENTIALS_ID + "', url: '" + HTTP_URL + "']],\n"
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
