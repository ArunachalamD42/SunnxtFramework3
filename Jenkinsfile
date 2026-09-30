pipeline {
    agent any

    stages {

        stage('Build') {
            steps {
                bat 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test -Dsurefire.suiteXmlFiles=testng-jenkins.xml'
            }
        }
    }
}