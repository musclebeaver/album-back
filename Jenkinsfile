pipeline {
  agent any

  environment {
    REGISTRY     = "ghcr.io"
    IMAGE_NS     = "musclebeaver"
    BACK_IMG     = "${REGISTRY}/${IMAGE_NS}/album-backend"
    TAG          = "prod-${env.BUILD_NUMBER}"
    COMPOSE_DIR  = "/app/album"   // 서버의 docker-compose.yml 위치
  }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  triggers {
    // 깃허브 웹훅을 쓰면 잡 설정에서 "GitHub hook trigger for GITScm polling" 체크
    // pollSCM('H/5 * * * *') // (옵션) 폴링 사용 시
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Docker Login (GHCR)') {
      steps {
        withCredentials([string(credentialsId: 'ghcr_pat', variable: 'CR_PAT')]) {
          sh 'echo "$CR_PAT" | docker login ${REGISTRY} -u ${IMAGE_NS} --password-stdin'
        }
      }
    }

    stage('Build Image (Gradle in Dockerfile)') {
      steps {
        sh '''
          docker build \
            -t ${BACK_IMG}:${TAG} \
            -t ${BACK_IMG}:latest \
            .
        '''
      }
    }

    stage('Push Image') {
      steps {
        sh '''
          docker push ${BACK_IMG}:${TAG}
          docker push ${BACK_IMG}:latest
        '''
      }
    }

    stage('Deploy (backend only)') {
      steps {
        sh '''
          cd ${COMPOSE_DIR}
          docker compose pull backend
          docker compose up -d backend
        '''
      }
    }
  }

  post {
    always {
      sh 'docker logout ${REGISTRY} || true'
    }
  }
}
