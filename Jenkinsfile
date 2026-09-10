pipeline {
    agent any
    tools {
        jdk 'JDK17'
        maven 'Maven'
    }
    stages {
        stage('Checkout') { steps { checkout scm } }
        stage('Clean') { steps { sh 'mvn clean' } }
        stage('Compile') { steps { sh 'mvn compile' } }
        stage('Test') {
            steps { sh 'mvn test' }
            post { always { junit 'target/surefire-reports/*.xml' } }
        }
        stage('Package') { steps { sh 'mvn package -DskipTests' } }
    }
    post {
        success { echo 'CI pipeline completed successfully.' }
        failure { echo 'CI pipeline failed.' }
    }
}
