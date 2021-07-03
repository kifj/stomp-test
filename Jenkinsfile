node {
  def mvnHome = tool 'Maven-3.6'
  env.JAVA_HOME = tool 'JDK-11'
  def branch = 'wildfly-24'
  def mavenSetting = 'dfe73d5e-dd12-4ed1-965f-7c8dcebd9101'

  stage('Checkout') {
    checkout scm
  }
  
  stage('Build') {
    withMaven(maven: 'Maven-3.6', mavenSettingsConfig: mavenSetting) {
      sh "mvn clean package"
    }
  }
  
  stage('Pre IT-Test') {
    withMaven(maven: 'Maven-3.6', mavenSettingsConfig: mavenSetting) {
      sh "mvn -Pdocker-integration-test pre-integration-test"
    }
  }

  stage('Run IT test') {
    docker
      .image('registry.x1/j7beck/x1-wildfly-stomp-test-it:1.6')
      .withRun('-e MANAGEMENT=public -e HTTP=public --name stomp-test-it') {
    c ->
      try {
        waitFor("http://${hostIp(c)}:9990/health/ready", 10, 6)
        withMaven(maven: 'Maven-3.6', mavenSettingsConfig: mavenSetting) {
          sh "mvn -Parq-jbossas-remote verify -Djboss.managementAddress=${hostIp(c)}"
        }
      } finally {
        junit '**/target/surefire-reports/TEST-*.xml'
        jacoco(execPattern: '**/**.exec')
      }
    }
  }
  
  stage('Publish') {
    withMaven(maven: 'Maven-3.6', mavenSettingsConfig: mavenSetting) {
      sh "mvn -Prpm deploy site-deploy -DskipTests"
      sh "mvn sonar:sonar -Dsonar.host.url=https://www.x1/sonar -Dsonar.projectKey=x1.wildfly:stomp-test:${branch} -Dsonar.projectName=stomp-test:${branch}"
    }
  }
  
  stage('Create image') {
    withMaven(maven: 'Maven-3.6', mavenSettingsConfig: mavenSetting) {
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
