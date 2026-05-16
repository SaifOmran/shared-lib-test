def call(Map config = [:]) {

    pipeline {

        agent any

        environment {

            SERVER_PORT = "${config.PORT}"

            IMAGE_NAME = "${config.IMAGE_NAME}"
            IMAGE_TAG  = "${config.IMAGE_TAG}"
            REPO_NAME  = "${config.REPO_NAME}"
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

            stage('Build') {

                steps {

                    sh 'mvn clean package'
                }
            }

            stage('Build Image') {

                steps {

                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."

                    sh """
                        docker tag \
                        ${IMAGE_NAME}:${IMAGE_TAG} \
                        ${REPO_NAME}:${IMAGE_TAG}
                    """
                }
            }

            stage('Push Image') {

                steps {

                    withCredentials([
                        usernamePassword(
                            credentialsId: 'docker-cred',
                            usernameVariable: 'USER',
                            passwordVariable: 'PASS'
                        )
                    ]) {

                        sh """
                            echo \$PASS | docker login \
                            -u \$USER \
                            --password-stdin
                        """

                        sh "docker push ${REPO_NAME}:${IMAGE_TAG}"
                    }
                }
            }
        }
    }
}
