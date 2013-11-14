name := "random-stuff"

version := "1.0"

scalaVersion := "2.9.3"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "0.8.0-incubating"

libraryDependencies += "org.apache.spark" %% "spark-core" % "0.8.0-incubating"

libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.2"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

mainClass := Some("GlitchTest")

fork in run := true

javaOptions in run += "-Dhello=kitty -XX:+PrintGC -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9000 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=aws-ds-cp-dev-tathagad-6001.iad6.amazon.com "


