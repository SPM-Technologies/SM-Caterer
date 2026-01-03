pipeline {
    agent any

    tools {
        maven 'Maven-3.9'    // Must match name in Jenkins > Manage Jenkins > Tools
        jdk 'JDK-17'         // Must match name in Jenkins > Manage Jenkins > Tools
    }

    stages {
        
        stage('Checkout') {
            steps {
                echo '📥 Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Compiling the project...'
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                echo '🧪 Running tests...'
                sh 'mvn test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                echo '📊 Generating coverage report...'
                sh 'mvn jacoco:report'
            }
        }

        stage('Package') {
            steps {
                echo '📦 Creating WAR file...'
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                }
            }
        }

        stage('Deploy') {
            steps {
                echo '🚀 Ready to deploy!'
                echo 'WAR file location: target/SM-Caterer-0.0.1-SNAPSHOT.war'
                // Add your deployment commands here later
            }
        }
    }

    post {
        success {
            echo '✅ Build Successful!'
        }
        failure {
            echo '❌ Build Failed!'
        }
    }
}
