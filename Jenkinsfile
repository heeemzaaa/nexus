pipeline {
    agent {
        node {
            label 'docker-agent'
        }
    }

    environment {
        NEXUS_HOST = 'nexus'
        // "localhost", not "nexus": docker push/pull are performed by the host's Podman service
        // behind the mounted socket, and the host can't resolve the "nexus" container name.
        NEXUS_DOCKER_REGISTRY_HOST = 'localhost'
        // Version of every Maven artifact and Docker image built by this run (read by scripts/*.sh)
        REVISION = "1.0.${env.BUILD_NUMBER}"
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
            <username>${env.NEXUS_USERNAME}</username>
            <password>${env.NEXUS_PASSWORD}</password>
        </server>
    </servers>
</settings>
'''
            }
        }

        stage('Build') {
            steps {
                echo 'Building..'
                sh 'MAVEN_SETTINGS="$WORKSPACE/.maven-settings.xml" NEXUS_HOST="$NEXUS_HOST" ./scripts/build.sh'
                sh '''
                cd frontend
                npm i
                ng build
                '''
            }
        }

        stage('Test') {
            steps {
                echo 'Testing..'
                sh 'MAVEN_SETTINGS="$WORKSPACE/.maven-settings.xml" NEXUS_HOST="$NEXUS_HOST" ./scripts/test.sh'
                sh 'npm install --save-dev @vitest/coverage-v8'
                sh 'cd frontend && ng test --watch=false --code-coverage'
            }
        }

        stage('SonarQube Analysis & Quality Gate - frontend') {
            steps {
                script {
                    dir('frontend') {
                        withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                            withSonarQubeEnv('sonar-server') {
                                sh 'npm install -g @sonar/scan@4.3.8'
                                sh '''sonar \
                                    -Dsonar.host.url=http://sonarqube:9000 \
                                    -Dsonar.projectKey=frontend \
                                    -Dsonar.javascript.lcov.reportPaths=coverage/lcov.info \
                                    -Dsonar.token=\$SONAR_TOKEN
                                '''
                            }
                        }

                        timeout(time: 5, unit: 'MINUTES') {
                                waitForQualityGate abortPipeline: true
                        }
                    }
                }
            }
        }

        stage('SonarQube Analysis & Quality Gate - backend') {
            steps {
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    script {
                        def services = ['product-service', 'user-service', 'media-service', 'api-gateway', 'order-service']
                        for (svc in services) {
                            dir(svc) {
                                withSonarQubeEnv('sonar-server') {
                                    sh """
                                    ./mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                                        -s "$WORKSPACE/.maven-settings.xml" \
                                        -Dsonar.projectKey=${svc} \
                                        -Dsonar.projectName=${svc} \
                                        -Dsonar.host.url=http://sonarqube:9000 \
                                        -DNEXUS_HOST="$NEXUS_HOST" \
                                        -Drevision="$REVISION" \
                                        -Dsonar.token=\$SONAR_TOKEN
                                    """
                                }
                                timeout(time: 5, unit: 'MINUTES') {
                                    waitForQualityGate abortPipeline: true
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Publish Maven Artifacts to Nexus') {
            when {
                not {
                    changeRequest()
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    sh 'MAVEN_SETTINGS="$WORKSPACE/.maven-settings.xml" NEXUS_HOST="$NEXUS_HOST" ./scripts/deploy_to_nexus.sh'
                }
            }
        }

        stage('Publish Docker Images to Nexus') {
            when {
                not {
                    changeRequest()
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    // Each image gets two tags: $GIT_COMMIT (traceability, used by Deliver/rollback) and $REVISION (same as the Maven version)
                    sh 'NEXUS_DOCKER_PUSH_REGISTRY="$NEXUS_DOCKER_REGISTRY_HOST:8082" NEXUS_DOCKER_PULL_REGISTRY="$NEXUS_DOCKER_REGISTRY_HOST:8083" IMAGE_TAG="$GIT_COMMIT" VERSION="$REVISION" ./scripts/publish_docker_to_nexus.sh'
                }
            }
        }

        stage('Deliver') {
            when {
                not {
                    changeRequest()
                }
            }
            steps {
                withCredentials([
                    file(credentialsId: 'buy01-env-file', variable: 'ENV_FILE'),
                    file(credentialsId: 'ssl-cert', variable: 'SSL_CERT'),
                    file(credentialsId: 'ssl-key', variable: 'SSL_KEY'),
                    file(credentialsId: 'ssl-passphrase', variable: 'SSL_PASSPHRASE'),
                    usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')
                ]) {
                    echo 'Deliver....'

                    sh '''
                        cp "$ENV_FILE" .env
                        cp "$SSL_CERT" frontend/secureCertificate.crt
                        cp "$SSL_KEY" frontend/private.key
                        cp "$SSL_PASSPHRASE" frontend/securePassphrase
                    '''

                    script {
                        try {
                            sh '''
                                trap 'docker logout "$NEXUS_DOCKER_REGISTRY_HOST:8083" >/dev/null 2>&1 || true' EXIT
                                export NEXUS_DOCKER_PULL_REGISTRY="$NEXUS_DOCKER_REGISTRY_HOST:8083"
                                export IMAGE_TAG="$GIT_COMMIT"
                                printf '%s' "$NEXUS_PASSWORD" | docker login "$NEXUS_DOCKER_REGISTRY_HOST:8083" --username "$NEXUS_USERNAME" --password-stdin
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
                                cp "$SSL_CERT" frontend/secureCertificate.crt
                                cp "$SSL_KEY" frontend/private.key
                                cp "$SSL_PASSPHRASE" frontend/securePassphrase

                                export NEXUS_DOCKER_PULL_REGISTRY="$NEXUS_DOCKER_REGISTRY_HOST:8083"
                                export IMAGE_TAG="$GIT_PREVIOUS_SUCCESSFUL_COMMIT"
                                printf '%s' "$NEXUS_PASSWORD" | docker login "$NEXUS_DOCKER_REGISTRY_HOST:8083" --username "$NEXUS_USERNAME" --password-stdin
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
                    to: 'elasriyoussef604@gmail.com',
                    subject: "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: "Build passed.\nLogs: ${env.BUILD_URL}"
                )
            }
        }
        failure {
            setBuildStatus('Build failed', 'FAILURE')
            script {
                mail(
                    to: 'elasriyoussef604@gmail.com',
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
        reposSource: [$class: 'ManuallyEnteredRepositorySource', url: 'https://github.com/yelasri07/cartify'],
        contextSource: [$class: 'ManuallyEnteredCommitContextSource', context: 'ci/jenkins/build-status'],
        errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']],
        statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: message, state: state]]]
    ])
}
