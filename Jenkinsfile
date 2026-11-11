pipeline {
    // The controller checks out the code once; every stage below runs in its own
    // ready-made Docker image on that same workspace (reuseNode true).
    agent any

    environment {
        NEXUS_HOST = 'nexus'
        // "localhost", not "nexus": docker push/pull are performed by the host's Podman service
        // behind the mounted socket, and the host can't resolve the "nexus" container name.
        NEXUS_DOCKER_REGISTRY_HOST = 'localhost'
        // Version of every Maven artifact and Docker image built by this run (read by scripts/*.sh)
        REVISION = "1.0.${env.BUILD_NUMBER}"
        // Nexus login for every stage (anonymous access is disabled): gives NEXUS_USR and NEXUS_PSW
        NEXUS = credentials('nexus-credentials')
    }

    stages {
        stage('Configure Maven for Nexus') {
            steps {
                writeFile file: '.maven-settings.xml', text: '''<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">
    <mirrors>
        <mirror>
            <id>nexus</id>
            <mirrorOf>*</mirrorOf>
            <url>http://nexus:8081/repository/maven-public/</url>
        </mirror>
    </mirrors>
    <servers>
        <server>
            <id>nexus</id>
            <username>${env.NEXUS_USR}</username>
            <password>${env.NEXUS_PSW}</password>
        </server>
    </servers>
</settings>
'''
            }
        }

        stage('Build - backend') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    // ci-net: reach "nexus"; jenkins-m2: keep the Maven cache between builds
                    args '--network ci-net -v jenkins-m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                sh 'MAVEN_SETTINGS="$WORKSPACE/.maven-settings.xml" NEXUS_HOST="$NEXUS_HOST" ./scripts/build.sh'
            }
        }

        stage('Build - frontend') {
            agent {
                docker {
                    image 'node:22'
                    args '-v jenkins-npm:/root/.npm'
                    reuseNode true
                }
            }
            steps {
                sh '''
                cd frontend
                npm ci
                npx ng build
                '''
            }
        }

        stage('Test - backend') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args '--network ci-net -v jenkins-m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                sh 'MAVEN_SETTINGS="$WORKSPACE/.maven-settings.xml" NEXUS_HOST="$NEXUS_HOST" ./scripts/test.sh'
            }
        }

        stage('Test - frontend') {
            agent {
                docker {
                    image 'node:22'
                    args '-v jenkins-npm:/root/.npm'
                    reuseNode true
                }
            }
            steps {
                sh 'cd frontend && npx ng test --watch=false --code-coverage'
            }
        }

        stage('Publish Maven Artifacts to Nexus') {
            when {
                not {
                    changeRequest()
                }
            }
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args '--network ci-net -v jenkins-m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                sh 'MAVEN_SETTINGS="$WORKSPACE/.maven-settings.xml" NEXUS_HOST="$NEXUS_HOST" ./scripts/deploy_to_nexus.sh'
            }
        }

        stage('Publish Docker Images to Nexus') {
            when {
                not {
                    changeRequest()
                }
            }
            agent {
                docker {
                    image 'docker:27-cli'
                    // the host's Podman socket, so docker build/push run on the host
                    // DOCKER_BUILDKIT=0: Podman does not support BuildKit, use the classic builder
                    args "-v ${env.HOST_DOCKER_SOCK}:/var/run/docker.sock -e DOCKER_BUILDKIT=0"
                    reuseNode true
                }
            }
            steps {
                // the scripts are written in bash, which docker:cli (Alpine) does not include
                sh 'apk add --no-cache bash git'
                // Credentials come from the pipeline-level NEXUS binding (NEXUS_USR/NEXUS_PSW): it exists before the
                // container starts, so it is passed on "docker run". Variables added later by withCredentials are only
                // passed on "docker exec", which the host's Podman does not forward, so they arrive empty.
                // Each image gets two tags: $GIT_COMMIT (traceability, used by Deliver/rollback) and $REVISION (same as the Maven version)
                sh 'NEXUS_USERNAME="$NEXUS_USR" NEXUS_PASSWORD="$NEXUS_PSW" NEXUS_DOCKER_PUSH_REGISTRY="$NEXUS_DOCKER_REGISTRY_HOST:8082" NEXUS_DOCKER_PULL_REGISTRY="$NEXUS_DOCKER_REGISTRY_HOST:8083" IMAGE_TAG="$GIT_COMMIT" VERSION="$REVISION" ./scripts/publish_docker_to_nexus.sh'
            }
        }

        stage('Deliver') {
            when {
                not {
                    changeRequest()
                }
            }
            agent {
                docker {
                    image 'docker:27-cli'
                    args "-v ${env.HOST_DOCKER_SOCK}:/var/run/docker.sock -e DOCKER_BUILDKIT=0"
                    reuseNode true
                }
            }
            steps {
                sh 'apk add --no-cache git'
                withCredentials([
                    file(credentialsId: 'buy01-env-file', variable: 'ENV_FILE'),
                    file(credentialsId: 'ssl-cert', variable: 'SSL_CERT'),
                    file(credentialsId: 'ssl-key', variable: 'SSL_KEY'),
                    file(credentialsId: 'ssl-passphrase', variable: 'SSL_PASSPHRASE')
                ]) {
                    echo 'Deliver....'

                    sh '''
                        cp "$ENV_FILE" .env
                        install -m 644 "$SSL_CERT" frontend/secureCertificate.crt
                        install -m 644 "$SSL_KEY" frontend/private.key
                        install -m 644 "$SSL_PASSPHRASE" frontend/securePassphrase
                    '''

                    script {
                        try {
                            sh '''
                                trap 'docker logout "$NEXUS_DOCKER_REGISTRY_HOST:8083" >/dev/null 2>&1 || true' EXIT
                                export NEXUS_DOCKER_PULL_REGISTRY="$NEXUS_DOCKER_REGISTRY_HOST:8083"
                                export IMAGE_TAG="$GIT_COMMIT"
                                printf '%s' "$NEXUS_PSW" | docker login "$NEXUS_DOCKER_REGISTRY_HOST:8083" --username "$NEXUS_USR" --password-stdin
                                docker compose -p cartify pull discovery-service api-gateway user-service product-service media-service order-service angular-app
                                docker compose -p cartify down
                                docker compose -p cartify up -d --no-build
                                docker compose ps
                            '''
                        } catch (err) {
                            if (!env.GIT_PREVIOUS_SUCCESSFUL_COMMIT) {
                                error 'Deploy failed and no previous successful commit exists to roll back to. Manual intervention required.'
                            }

                            echo "Deployment failed — rolling back to ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}"
                            sh '''
                                trap 'docker logout "$NEXUS_DOCKER_REGISTRY_HOST:8083" >/dev/null 2>&1 || true' EXIT
                                git checkout ${GIT_PREVIOUS_SUCCESSFUL_COMMIT}

                                cp "$ENV_FILE" .env
                                install -m 644 "$SSL_CERT" frontend/secureCertificate.crt
                                install -m 644 "$SSL_KEY" frontend/private.key
                                install -m 644 "$SSL_PASSPHRASE" frontend/securePassphrase

                                export NEXUS_DOCKER_PULL_REGISTRY="$NEXUS_DOCKER_REGISTRY_HOST:8083"
                                export IMAGE_TAG="$GIT_PREVIOUS_SUCCESSFUL_COMMIT"
                                printf '%s' "$NEXUS_PSW" | docker login "$NEXUS_DOCKER_REGISTRY_HOST:8083" --username "$NEXUS_USR" --password-stdin
                                docker compose -p cartify pull discovery-service api-gateway user-service product-service media-service order-service angular-app
                                docker compose -p cartify down
                                docker compose -p cartify up -d --no-build
                            '''
                            error "Deployment failed, rolled back to previous successful commit ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}"
                        } finally {
                            sh '''
                                rm -f .env
                                rm -f frontend/secureCertificate.crt
                                rm -f frontend/private.key
                                rm -f frontend/securePassphrase
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'rm -f .maven-settings.xml'
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: '**/target/surefire-reports/*.xml, frontend/coverage/**', allowEmptyArchive: true
        }
        success {
            setBuildStatus('Build succeeded', 'SUCCESS')
            script {
                mail(
                    to: 'hamzaelkhawlani00@gmail.com',
                    subject: "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: "Build passed.\nLogs: ${env.BUILD_URL}"
                )
            }
        }
        failure {
            setBuildStatus('Build failed', 'FAILURE')
            script {
                mail(
                    to: 'hamzaelkhawlani00@gmail.com',
                    subject: "❌ FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: "Build failed.\nLogs: ${env.BUILD_URL}console"
                )
            }
        }
    }
}

void setBuildStatus(String message, String state) {
    step([
        $class: 'GitHubCommitStatusSetter',
        reposSource: [$class: 'ManuallyEnteredRepositorySource', url: 'https://github.com/heeemzaaa/nexus'],
        contextSource: [$class: 'ManuallyEnteredCommitContextSource', context: 'ci/jenkins/build-status'],
        errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']],
        statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: message, state: state]]]
    ])
}
