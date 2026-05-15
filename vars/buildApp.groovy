def call(Map config = [:]) {

    pipeline {

        agent any

        environment {
            SERVER_PORT = "${config.PORT}"
        }

        tools {
            maven 'mvn'
            jdk 'jdk'
        }

        stages {

            stage('Clone') {

                steps {

                    git branch: 'main',
                    url: "${config.REPO_URL}"

                }
            }

            stage('Compile') {

                steps {
                    sh 'mvn clean compile'
                }
            }

            stage('Build') {

                steps {
                    sh 'mvn test'
                }
            }

            stage('Package') {

                steps {
                    sh 'mvn package'
                }
            }

            stage('Run') {

                steps {

                    sh '''
                        nohup java -jar target/*.jar \
                        > app.log 2>&1 &
                        
                        sleep 300
                    '''

                }
            }
        }
    }
}
