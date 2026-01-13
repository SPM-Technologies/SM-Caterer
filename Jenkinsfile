pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        APP_NAME = 'SM-Caterer'
        ARTIFACT_NAME = 'SM-Caterer-0.0.1-SNAPSHOT.war'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Compiling the project...'
                bat 'mvn clean compile -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                bat 'mvn test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                echo 'Generating coverage report...'
                bat 'mvn jacoco:report'
            }
            post {
                always {
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Creating WAR file...'
                bat 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                }
            }
        }

        stage('Verify') {
            steps {
                echo 'Running verification checks...'
                bat 'mvn verify -DskipTests'
            }
        }

        stage('Deploy Info') {
            steps {
                echo '========================================='
                echo 'DEPLOYMENT INFORMATION'
                echo '========================================='
                echo "Artifact: target/${ARTIFACT_NAME}"
                echo ''
                echo 'To deploy to production:'
                echo '1. Copy WAR to server'
                echo '2. Restart service: sudo systemctl restart cloudcaters'
                echo '3. Verify health: curl http://server:8080/actuator/health'
                echo ''
                echo 'For production profile, use:'
                echo '  java -jar app.war --spring.profiles.active=prod'
                echo '========================================='
            }
        }
    }

    post {
        success {
            echo 'Build Successful!'
            echo "Artifact ready: target/${ARTIFACT_NAME}"
        }
        failure {
            echo 'Build Failed!'
        }
        always {
            cleanWs(cleanWhenNotBuilt: false,
                    deleteDirs: true,
                    disableDeferredWipeout: true,
                    notFailBuild: true,
                    patterns: [[pattern: 'target/**', type: 'EXCLUDE']])
        }
    }
}
