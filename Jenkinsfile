pipeline {
  agent any

  environment {
    branch = 'wildfly-10'
    wildfly = '/opt/wildfly-10.1.0.Final'
    mvnHome = tool 'Maven-3.5'
  }

  stages {
    stage('Build') {
      steps {
        sh '${mvnHome}/bin/mvn clean package'
      }
      post {
        success {
          archiveArtifacts(artifacts: '**/target/*.war', allowEmptyArchive: true)
        }
      }
    }
    stage('Pre IT-Test') {
      steps {
        sh "${mvnHome}/bin/mvn -Pdocker-integration-test pre-integration-test"
      }
    }
    stage('Test') {
      steps {
        lock("local-server") {
          sh '${mvnHome}/bin/mvn verify -Parq-jbossas-managed -Djboss.home=${wildfly}'
        }
      }
      post {
        always {
          junit '**/target/surefire-reports/TEST-*.xml'
        }
        success {
          archiveArtifacts(artifacts: '**/target/*.war', allowEmptyArchive: true)
        }
      }
    }
    stage('Publish') {
      steps {
          sh '${mvnHome}/bin/mvn -Prpm deploy site-deploy -DskipTests'
          sh '${mvnHome}/bin/mvn sonar:sonar -Dsonar.host.url=https://www.x1/sonar -Dsonar.branch=${branch}'
      }
    }
    stage('Build image') {
      steps {
        sh "${mvnHome}/bin/mvn -Pdocker install"
      }
    }
  }
}
