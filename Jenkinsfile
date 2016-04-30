node {
   def mvnHome = tool 'Maven-3.3'
   
   stage 'Checkout'
   git url: 'https://github.com/kifj/stomp-test.git'

   stage 'Build'
   sh "${mvnHome}/bin/mvn clean package"
   
   stage 'Test'
   sh "${mvnHome}/bin/mvn verify -Parq-jbossas-managed -Djboss.home=/opt/wildfly-10.0.0.Final"
   
   stage 'Publish'
   sh "${mvnHome}/bin/mvn -Prpm deploy site-deploy -DskipTests" 
}
