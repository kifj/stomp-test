node {
  def mvnHome = tool 'Maven-3.3'
  
  stage('Checkout') {
    git url: 'https://github.com/kifj/stomp-test.git', branch: 'wildfly-11'
  }
  
  stage('Build') {
    sh "${mvnHome}/bin/mvn clean package"
  }
  
  stage('Prepare IT test') {
    sh "${mvnHome}/bin/mvn -Pdocker-integration-test pre-integration-test"
  }
  
  stage('Run IT test') {
    docker
      .image('j7beck/x1-wildfly-stomp-test-it:1.5')
      .withRun('-e MANAGEMENT=public -e HTTP=public --name stomp-test-it') {
    c ->
      try {
        sleep 60
        sh "${mvnHome}/bin/mvn -Parq-jbossas-remote verify -Djboss.managementAddress=${hostIp(c)}"
      } finally {
        junit '**/target/surefire-reports/TEST-*.xml'
      }
    }
  }
  
  stage('Publish') {
    sh "${mvnHome}/bin/mvn -Prpm deploy site-deploy -DskipTests"
  }
  
  stage('Create image') {
    sh "${mvnHome}/bin/mvn -Pdocker install"
  }
}

def hostIp(container) {
  sh "docker inspect -f {{.NetworkSettings.IPAddress}} ${container.id} > hostIp"
  readFile('hostIp').trim()
}
