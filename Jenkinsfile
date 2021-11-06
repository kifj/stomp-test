pipeline {
  agent any

  environment {
    branch = 'wildfly-10'
    wildfly = '/opt/wildfly-10.1.0.Final'
  }

  tools { 
    maven 'Maven-3.6' 
    jdk 'JDK-1.8' 
  }

  stages {
    stage('Build') {
      steps {
        sh 'mvn clean package'
      }
      post {
        success {
          archiveArtifacts(artifacts: '**/target/*.war', allowEmptyArchive: true)
        }
      }
    }
    stage('Pre IT-Test') {
      steps {
        sh "mvn -Pdocker-integration-test pre-integration-test"
      }
    }
    stage('Test') {
      steps {
        lock("local-server") {
          sh 'mvn verify -Parq-jbossas-managed -Djboss.home=${wildfly}'
        }
      }
      post {
        always {
          junit '**/target/surefire-reports/TEST-*.xml'
          jacoco(execPattern: '**/**.exec')
        }
        success {
          archiveArtifacts(artifacts: '**/target/*.war', allowEmptyArchive: true)
        }
      }
    }
    stage('Publish') {
      steps {
          sh 'mvn -Prpm deploy site-deploy -DskipTests'
          withEnv(["JAVA_HOME=${tool 'JDK-11'}"]) {
            sh 'mvn sonar:sonar -Dsonar.host.url=https://www.x1/sonar -Dsonar.projectKey=x1.wildfly:stomp-test:${branch} -Dsonar.coverage.exclusions="**/*.js"'
          }
      }
    }
    stage('Build image') {
      steps {
        sh "mvn -Pdocker install"
      }
    }
  }
}
