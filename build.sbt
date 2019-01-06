
lazy val commonSettings = Seq(
  name := "KeycloakUI",
  version := "0.1",
  scalaVersion := "2.12.7")

lazy val app = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    mainClass in assembly := Some("com.github.rkbalgi.apps.keycloak.ui.KeycloakUI")
  )

//resolvers+=Resolver.typesafeIvyRepo("releases")
resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"

libraryDependencies += "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-json-provider" % "2.9.7"
libraryDependencies += "com.github.fge" % "json-patch" % "1.9"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "org.jboss.spec.javax.servlet" % "jboss-servlet-api_3.1_spec" % "1.0.2.Final"
libraryDependencies += "junit" % "junit" % "4.12"
libraryDependencies += "org.jboss.logging" % "jboss-logging" % "3.3.2.Final"
libraryDependencies += "org.jboss.logging" % "jboss-logging-annotations" % "2.1.0.Final"
libraryDependencies += "org.jboss.logging" % "jboss-logging-processor" % "2.1.0.Final"
libraryDependencies += "javax.activation" % "activation" % "1.1.1"
libraryDependencies += "net.jcip" % "jcip-annotations" % "1.0"
libraryDependencies += "commons-io" % "commons-io" % "2.5"
libraryDependencies += "javax.validation" % "validation-api" % "2.0.1.Final"
//libraryDependencies += "org.codehaus.jackson" % "jackson-core-asl" % "1.9.13"
//libraryDependencies += "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.13"
//libraryDependencies += "org.codehaus.jackson" % "jackson-jaxrs" % "1.9.13"
//libraryDependencies += "org.codehaus.jackson" % "jackson-xc" % "1.9.13"
libraryDependencies += "org.jboss.spec.javax.ws.rs" % "jboss-jaxrs-api_2.1_spec" % "1.0.1.Final"
libraryDependencies += "org.jboss.spec.javax.xml.bind" % "jboss-jaxb-api_2.3_spec" % "1.0.0.Final"
libraryDependencies += "org.reactivestreams" % "reactive-streams" % "1.0.2"
libraryDependencies += "org.jboss.spec.javax.annotation" % "jboss-annotations-api_1.2_spec" % "1.0.0.Final"
libraryDependencies += "javax.json.bind" % "javax.json.bind-api" % "1.0"
libraryDependencies += "org.jboss.resteasy" % "resteasy-jaxrs" % "3.6.1.SP2"
libraryDependencies += "org.keycloak" % "keycloak-authz-client" % "4.5.0.Final"
libraryDependencies += "org.keycloak" % "keycloak-admin-client" % "4.5.0.Final"
libraryDependencies += "com.google.guava" % "guava" % "26.0-jre"
//libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.2"
libraryDependencies += "org.jboss.resteasy" % "resteasy-client" % "3.6.1.SP2"
libraryDependencies += "org.jboss.resteasy" % "resteasy-jackson2-provider" % "3.6.1.SP2"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.10"


