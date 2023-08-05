node {
  def mvnHome = tool 'Maven-3.9'
  env.JAVA_HOME = tool 'JDK-17'
  def branch = 'wildfly-29'
  def mavenSetting = 'dfe73d5e-dd12-4ed1-965f-7c8dcebd9101'

  stage('Checkout') {
    checkout scm
  }
  
  stage('Build') {
    withMaven(maven: 'Maven-3.9', mavenSettingsConfig: mavenSetting, options: [jacocoPublisher(disabled: true), junitPublisher(disabled: true)]) {
      sh "mvn clean package"
    }
  }
  
  stage('Pre IT-Test') {
    withMaven(maven: 'Maven-3.9', mavenSettingsConfig: mavenSetting, options: [jacocoPublisher(disabled: true), junitPublisher(disabled: true)]) {
      sh "mvn -Pdocker-integration-test pre-integration-test"
    }
  }

  stage('Run IT test') {
    docker
      .image('registry.x1/j7beck/x1-wildfly-stomp-test-it:1.8')
      .withRun('-e MANAGEMENT=public -e HTTP=public --name stomp-test-it') {
    c -> 
        waitFor("http://${hostIp(c)}:9990/health/ready", 20, 3)
        withMaven(maven: 'Maven-3.9', mavenSettingsConfig: mavenSetting) {
          sh "mvn -Parq-remote verify -Djboss.managementAddress=${hostIp(c)}"
	}      
    }
  }
  
  stage('Publish') {
    withMaven(maven: 'Maven-3.9', mavenSettingsConfig: mavenSetting, options: [jacocoPublisher(disabled: true), junitPublisher(disabled: true)]) {
      withCredentials([usernameColonPassword(credentialsId: 'nexus', variable: 'USERPASS')]) {
        sh '''
          mvn -Prpm deploy site-deploy -DskipTests
          mvn sonar:sonar -Dsonar.host.url=https://www.x1/sonar -Dsonar.projectKey=x1.wildfly:stomp-test:wildfly-29 -Dsonar.projectName=stomp-test:wildfly-29
          curl -u "$USERPASS" --upload-file target/rpm/stomp-test-v*/RPMS/noarch/stomp-test-*.noarch.rpm https://www.x1/nexus/repository/x1-extra-rpms/testing/
        '''        
      }
    }
  }
  
  stage('Create image') {
    withMaven(maven: 'Maven-3.9', mavenSettingsConfig: mavenSetting, options: [jacocoPublisher(disabled: true), junitPublisher(disabled: true)]) {
      sh "mvn -Pdocker clean install k8s:push"
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
