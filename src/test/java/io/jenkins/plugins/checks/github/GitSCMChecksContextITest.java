package io.jenkins.plugins.checks.github;

import hudson.model.Action;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GitSCMChecksContext}.
 */
@WithJenkins
class GitSCMChecksContextITest {
    private static final String EXISTING_HASH = "4ecc8623b06d99d5f029b66927438554fdd6a467";
    private static final String HTTP_URL = "https://github.com/jenkinsci/github-checks-plugin.git";
    private static final String CREDENTIALS_ID = "credentials";
    private static final String URL_NAME = "url";

    /**
     * Creates a FreeStyle job that uses {@link hudson.plugins.git.GitSCM} and runs a successful build.
     * Then this build is used to create a new {@link GitSCMChecksContext}. So the build actually is not publishing
     * the checks we just ensure that we can create the context with the successful build (otherwise we would need
     * Wiremock to handle the requests to GitHub).
     */
    @Test
    void shouldRetrieveContextFromFreeStyleBuild(JenkinsRule j) throws Exception {
        FreeStyleProject job = j.createFreeStyleProject();

        BranchSpec branchSpec = new BranchSpec(EXISTING_HASH);
        GitSCM scm = new GitSCM(GitSCM.createRepoList(HTTP_URL, CREDENTIALS_ID),
                Collections.singletonList(branchSpec),
                null, null, Collections.emptyList());
        job.setScm(scm);

        Run<?, ?> run = buildSuccessfully(j, job);

        GitSCMChecksContext gitSCMChecksContext = new GitSCMChecksContext(run, URL_NAME);

        assertThat(gitSCMChecksContext.getRepository()).isEqualTo("jenkinsci/github-checks-plugin");
        assertThat(gitSCMChecksContext.getHeadSha()).isEqualTo(EXISTING_HASH);
        assertThat(gitSCMChecksContext.getCredentialsId()).isEqualTo(CREDENTIALS_ID);
    }

    private Run<?, ?> buildSuccessfully(JenkinsRule j, ParameterizedJobMixIn.ParameterizedJob<?, ?> job) throws Exception {
        return j.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0, new Action[0]));
    }

    /**
     * Creates a pipeline that uses {@link hudson.plugins.git.GitSCM} and runs a successful build.
     * Then this build is used to create a new {@link GitSCMChecksContext}.
     */
    @Test
    void shouldRetrieveContextFromPipeline(JenkinsRule j) throws Exception {
        WorkflowJob job = j.createProject(WorkflowJob.class);

        job.setDefinition(new CpsFlowDefinition("node {\n"
                + "  stage ('Checkout') {\n"
                + "    checkout scm: ([\n"
                + "                    $class: 'GitSCM',\n"
                + "                    userRemoteConfigs: [[credentialsId: '" + CREDENTIALS_ID + "', url: '" + HTTP_URL + "']],\n"
                + "                    branches: [[name: '" + EXISTING_HASH + "']]\n"
                + "            ])"
                + "  }\n"
                + "}\n", true));

        Run<?, ?> run = buildSuccessfully(j, job);

        GitSCMChecksContext gitSCMChecksContext = new GitSCMChecksContext(run, URL_NAME);

        assertThat(gitSCMChecksContext.getRepository()).isEqualTo("jenkinsci/github-checks-plugin");
        assertThat(gitSCMChecksContext.getCredentialsId()).isEqualTo(CREDENTIALS_ID);
        assertThat(gitSCMChecksContext.getHeadSha()).isEqualTo(EXISTING_HASH);
    }
}
