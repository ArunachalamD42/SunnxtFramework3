pipeline {
    agent any

    parameters {
        choice(
            name: 'TEST_SUITE',
            choices: ['testng-jenkins.xml', 'testng.xml', 'testng-login.xml'],
            description: 'Select the TestNG suite to execute'
        )
    }

    stages {

        stage('Build') {
            steps {
                bat 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                bat "mvn test -Dsurefire.suiteXmlFiles=${params.TEST_SUITE}"
            }
        }
    }
}