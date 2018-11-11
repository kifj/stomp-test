pipeline {
  agent any

  environment {
    branch = 'wildfly-14'
    wildfly = '/opt/wildfly-14.0.0.Final'
    mvnHome = tool 'Maven-3.6'
  }

  stages {
    stage('Build') {
      agent {
        docker {
          reuseNode true
          image 'j7beck/x1-maven3:3.6.0'
          args '-u maven:docker -v maven-data:/home/maven/.m2 ' 
        }
      }
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
        sh "${mvnHome}/bin/mvn -Pdocker-integration-test pre-integration-test"
      }
    }
    stage('Test') {
      agent {
        docker {
          reuseNode true
          image 'j7beck/x1-maven3:3.6.0'
          args '-v maven-data:/home/maven/.m2 ' 
        }
      }
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
      agent {
        docker {
          reuseNode true
          image 'j7beck/x1-maven3:3.6.0'
          args '-v maven-data:/home/maven/.m2 ' 
        }
      }
      steps {
          sh 'mvn -Prpm deploy site-deploy -DskipTests'
          sh 'mvn sonar:sonar -Dsonar.host.url=https://www.x1/sonar -Dsonar.branch=${branch} -Dsonar.coverage.exclusions="**/*.js"'
      }
    }
    stage('Build image') {
      steps {
        sh "${mvnHome}/bin/mvn -Pdocker install"
      }
    }
  }
}
