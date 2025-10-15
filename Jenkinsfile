pipeline {
  agent any
  environment {
    SONAR_HOST_URL = 'http://localhost:9000'
    DOCKER_BUILDKIT = '1'
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build JAR (inside Dockerfile)') {
      steps {
        // Nothing to do here on the host; the Dockerfile builds the JAR.
        echo 'The multi-stage Docker build will compile the project.'
      }
    }
    stage('Build Docker Image') {
      steps {
        sh 'docker compose build app'
      }
    }
    stage('SonarQube Analysis') {
      when { expression { return env.SONAR_HOST_URL != null } }
      steps {
        withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
          sh """
            chmod +x mvnw
            ./mvnw -B -DskipTests verify sonar:sonar \
              -Dsonar.host.url=${SONAR_HOST_URL} \
              -Dsonar.login=${SONAR_TOKEN}
          """
        }
      }
    }
    stage('Deploy with Compose') {
      steps {
        sh 'docker compose up -d app'
      }
    }
  }
  post {
    success {
      sh 'docker ps --format "{{.Names}} -> {{.Image}}"'
      echo 'Deployment complete.'
    }
  }
}
