@Library('jenkins-shared-library@latest') _

pipeline {
    agent {
        kubernetes {
            label 'jenkins-maven-agent-jdk11'
        }
    }

    environment {
        MVN_VERSION = "maven 3.6.1"
        PACT_BROKER_URL = "https://pocbackbase.pact.dius.com.au"
        PACT_BROKER_CREDS = credentials('pactbroker_auth_token')
        GIT_HASH_SHORT = GIT_COMMIT.take(7)
        BRANCH = GIT_BRANCH.substring(GIT_BRANCH.indexOf("/") + 1)
    }

    parameters {
        string defaultValue: '', description: 'What Pact consumer would you like to verify.', name: 'PACT_CONSUMER', trim: true
        string defaultValue: '', description: 'What Pact consumer tag would you like to verify.', name: 'PACT_CONSUMER_TAG', trim: true
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    ciSCMCheckout()
                }
            }
        }
    
        stage('Verify the pact for a consumer') {
            steps {
                withMaven(maven: MVN_VERSION,  mavenSettingsConfig: SETTINGS_ID) {
                    /*
                      This maven command runs Pact provider verification.
                         pactbroker.url                              : for broker location
                         pactbroker.auth.token                       : for broker credentials
                         pact.commithash                             : commit hash used in the version number
                         pactbroker.consumerversionselectors.consumer: verify only this consumer
                         pactbroker.consumerversionselectors.tags    : verify only these tags (for selected consumer)
                         pact.verifier.publishResults                : whether to publish results back to the broker
                         pact.provider.tag                           : the provider tag to use when publishing results
                         groups (PactProvider)                       : ensure only Pact Provider tests are run in maven test phase
                    */
                    sh """
                        mvn test \
                           -Dpactbroker.url=${PACT_BROKER_URL} \
                           -Dpactbroker.auth.token=${PACT_BROKER_CREDS} \
                           -Dpactbroker.consumerversionselectors.consumer=${PACT_CONSUMER} \
                           -Dpactbroker.consumerversionselectors.tags=${PACT_CONSUMER_TAG} \
                           -Dpact.verifier.publishResults=true \
                           -Dpact.commithash=#${GIT_HASH_SHORT} \
                           -Dpact.provider.tag=${BRANCH} \
                           -Dgroups=PactProvider
                    """
                }
            }
        }
    }

    post {
        failure {
            slackSend(channel: "#AC-jenkins", color: '#FF0000', message: "${env.JOB_NAME} has failed: ${env.BUILD_URL}")
        }
    }
}
