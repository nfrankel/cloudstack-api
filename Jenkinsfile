pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage ('Package') {
            steps {
                script {
                    docker.withRegistry('https://registry.internal.exoscale.ch') {
                        def java = docker.image('registry.internal.exoscale.ch/exoscale/maven:latest')
                        java.pull()
                        java.inside('-u root --net=host -v /home/exec/.m2:/root/.m2') {
                            sh 'mvn install'
                        }
                    }
                }
            }
        }
    }
}
