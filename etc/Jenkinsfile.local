node {
    def mvnHome = tool 'mvn-default'

    stage ('Checkout') {
        checkout scm
    }

    stage ('Build and Static Analysis') {
        withMaven(maven: 'mvn-default', mavenLocalRepo: '/var/data/m2repository', mavenOpts: '-Xmx768m -Xms512m') {
            sh 'mvn -ntp -V -e clean verify -Dmaven.test.failure.ignore -Dgpg.skip'
        }

        recordIssues tools: [java(), javaDoc()], aggregatingResults: 'true', id: 'java', name: 'Java'
        recordIssues tool: errorProne(), healthy: 1, unhealthy: 20

        junit testResults: '**/target/*-reports/TEST-*.xml'

        recordIssues tools: [checkStyle(pattern: 'target/checkstyle-result.xml'),
            spotBugs(pattern: 'target/spotbugsXml.xml'),
            pmdParser(pattern: 'target/pmd.xml'),
            cpd(pattern: 'target/cpd.xml'),
            taskScanner(highTags:'FIXME', normalTags:'TODO', includePattern: '**/*.java', excludePattern: 'target/**/*')],
            qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]]
    }

    stage ('Line and Branch Coverage') {
        withMaven(maven: 'mvn-default', mavenLocalRepo: '/var/data/m2repository', mavenOpts: '-Xmx768m -Xms512m') {
            sh "mvn -ntp -V -U -e jacoco:prepare-agent test jacoco:report -Dmaven.test.failure.ignore"
        }
        publishCoverage adapters: [jacocoAdapter('**/*/jacoco.xml')], sourceFileResolver: sourceFiles('STORE_ALL_BUILD')
    }

    stage ('Mutation Coverage') {
        withMaven(maven: 'mvn-default', mavenLocalRepo: '/var/data/m2repository', mavenOpts: '-Xmx768m -Xms512m') {
            sh "mvn -ntp org.pitest:pitest-maven:mutationCoverage"
        }
        step([$class: 'PitPublisher', mutationStatsFile: 'target/pit-reports/**/mutations.xml'])
    }

    stage ('Autograding') {
        autoGrade('{"analysis":{"maxScore":100,"errorImpact":-5,"highImpact":-2,"normalImpact":-1,"lowImpact":-1}, "tests":{"maxScore":100,"passedImpact":0,"failureImpact":-5,"skippedImpact":-1}, "coverage":{"maxScore":100,"coveredPercentageImpact":0,"missedPercentageImpact":-1}, "pit":{"maxScore":100,"detectedImpact":0,"undetectedImpact":0,"undetectedPercentageImpact":-1,"detectedPercentageImpact":0}}')
    }
}
