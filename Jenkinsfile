node {
  def mvnHome = tool 'Maven-3.6'
  env.JAVA_HOME = tool 'JDK-11'
  def branch = 'wildfly-19'

  stage('Checkout') {
    git url: 'ssh://git@github.com/kifj/stomp-test.git', branch: 'wildfly-19'
  }
  
  stage('Build') {
    sh "${mvnHome}/bin/mvn clean package"
  }
  
  stage('Pre IT-Test') {
    sh "${mvnHome}/bin/mvn -Pdocker-integration-test pre-integration-test"
  }

  stage('Run IT test') {
    docker
      .image('j7beck/x1-wildfly-stomp-test-it:1.6')
      .withRun('-e MANAGEMENT=public -e HTTP=public --name stomp-test-it') {
    c ->
      try {
        waitFor("http://${hostIp(c)}:8080", 10, 6)
        sh "${mvnHome}/bin/mvn -Parq-jbossas-remote verify -Djboss.managementAddress=${hostIp(c)}"
      } finally {
        junit '**/target/surefire-reports/TEST-*.xml'
        jacoco(execPattern: '**/**.exec')
      }
    }
  }
  
  stage('Publish') {
    sh "${mvnHome}/bin/mvn -Prpm deploy site-deploy -DskipTests"
    sh "${mvnHome}/bin/mvn sonar:sonar -Dsonar.host.url=https://www.x1/sonar -Dsonar.projectKey=x1.wildfly:stomp-test:${branch}"
  }
  
  stage('Create image') {
    sh "${mvnHome}/bin/mvn -Pdocker install fabric8:push"
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
