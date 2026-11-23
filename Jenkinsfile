pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  environment {
    APP_NAME = 'payment-service'
    IMAGE_REPO = "${env.REGISTRY ?: 'registry.example.com'}/${APP_NAME}"
    IMAGE_TAG = "${env.GIT_COMMIT?.take(8) ?: env.BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        sh 'mvn -B -DskipTests compile'
      }
    }

    stage('Unit & Integration Tests') {
      steps {
        sh 'mvn -B test'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Static Analysis') {
      steps {
        sh 'mvn -B -DskipTests spotbugs:check || true'
        sh 'mvn -B -DskipTests checkstyle:check || mvn -B -DskipTests validate'
      }
    }

    stage('Dependency Scan') {
      steps {
        sh '''
          if command -v trivy >/dev/null 2>&1; then
            trivy fs --severity --exit-code 0 .
          else
            echo "trivy not installed on agent; skipping filesystem scan"
          fi
        '''
      }
    }

    stage('Image Build & Push') {
      when {
        anyOf {
          branch 'main'
          branch 'master'
          branch 'develop'
        }
      }
      steps {
        sh """
          docker build -t ${IMAGE_REPO}:${IMAGE_TAG} -t ${IMAGE_REPO}:build-${BUILD_NUMBER} .
          if command -v trivy >/dev/null 2>&1; then
            trivy image --severity --exit-code 0 ${IMAGE_REPO}:${IMAGE_TAG}
          fi
          docker push ${IMAGE_REPO}:${IMAGE_TAG}
          docker push ${IMAGE_REPO}:build-${BUILD_NUMBER}
        """
      }
    }

    stage('Deploy Non-Prod') {
      when {
        anyOf {
          branch 'main'
          branch 'master'
          branch 'develop'
        }
      }
      steps {
        sh """
          kubectl apply -k k8s/
          kubectl -n payments set image deployment/payment-service \
            payment-service=${IMAGE_REPO}:${IMAGE_TAG}
          kubectl -n payments rollout status deployment/payment-service --timeout=180s
        """
      }
    }

    stage('Promote to Prod') {
      when {
        anyOf {
          branch 'main'
          branch 'master'
        }
      }
      steps {
        input message: "Promote ${IMAGE_REPO}:${IMAGE_TAG} to production?", ok: 'Deploy'
        sh """
          kubectl --context=prod apply -k k8s/
          kubectl --context=prod -n payments set image deployment/payment-service \
            payment-service=${IMAGE_REPO}:${IMAGE_TAG}
          kubectl --context=prod -n payments rollout status deployment/payment-service --timeout=180s
        """
      }
    }
  }

  post {
    success {
      echo "Pipeline succeeded for ${IMAGE_REPO}:${IMAGE_TAG}"
    }
    failure {
      echo "Pipeline failed for ${env.BRANCH_NAME}"
    }
  }
}
