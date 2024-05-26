pipeline {
  agent any
  tools { 
    maven 'Maven-3.9'
  }
  environment {
    artifactId = readMavenPom().getArtifactId()
    groupId = readMavenPom().getGroupId()
  }
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }
    stage('Build and Test') {
      agent {
        docker {
          image 'registry.x1/j7beck/x1-maven3:jdk-1.8.0'
          args '-u 994:967 -v $HOME/.m2/repository:/var/lib/jenkins/.m2/repository -v /var/run/docker.sock:/var/run/docker.sock -v /usr/bin/docker:/usr/bin/docker'
        }
      }
      stages {
        stage('Build') {
          steps {
            sh '$MAVEN_HOME/bin/mvn -B clean package'
          }
          post {
            success {
              archiveArtifacts(artifacts: '**/target/*.war', allowEmptyArchive: true)
            }
          }
        }
        stage('Pre IT-Test') {
          steps {
            sh "$MAVEN_HOME/bin/mvn -B -Pdocker-integration-test pre-integration-test -Dfabric8.logDeprecationWarning=false"
          }
        }
        stage('Test') {
          steps {
            script {
              docker
                .image('registry.x1/j7beck/x1-wildfly-stomp-test-it:1.4')
                .withRun('-e MANAGEMENT=public -e HTTP=public --name stomp-test-it') { c ->
                  waitFor("http://${hostIp(c)}:8080", 10, 6)
                  sh "$MAVEN_HOME/bin/mvn -Parq-jbossas-remote verify -Djboss.managementAddress=${hostIp(c)}"
              }
            }
          }
          post {
            always {
              junit '**/target/surefire-reports/TEST-*.xml'
              jacoco(execPattern: '**/**.exec')
            }
          }
        }
        stage('Build image') {
          steps {
            sh "$MAVEN_HOME/bin/mvn -B -Pdocker install fabric8:push -Dfabric8.logDeprecationWarning=false"
          }
        }
      }
    }
    stage('Publish') {
      tools {
        jdk 'JDK-17'
      }
      steps {
        sh 'mvn -B deploy site-deploy -DskipTests'
      }
      post {
        always {
          recordIssues tools: [spotBugs(pattern: 'target/spotbugsXml.xml')]
        }
      }
    }
    stage('Sonar') {
      tools {
        jdk 'JDK-17'
      }
      steps {
        sh "mvn sonar:sonar -DskipTests -Dsonar.java.coveragePlugin=jacoco -Dsonar.jacoco.reportPath=target/jacoco.exec -Dsonar.host.url=https://www.x1/sonar -Dsonar.projectKey=${groupId}:${artifactId}:wildfly-10 -Dsonar.projectName=${artifactId}:wildfly-10"
      }
    }
  }
}

def hostIp(container) {
  sh "docker inspect -f {{.NetworkSettings.IPAddress}} ${container.id} > hostIp"
  readFile('hostIp').trim()
}

def waitFor(target, sleepInSec, retries) {
  retry (retries) {
    sleep sleepInSec
    httpRequest url: target, validResponseCodes: '200'
  }
}

