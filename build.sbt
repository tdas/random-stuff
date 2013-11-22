name := "random-stuff"

version := "1.0"

scalaVersion := "2.9.3"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % "0.9.0-incubating-SNAPSHOT",
  "org.apache.spark" %% "spark-core" % "0.9.0-incubating-SNAPSHOT",
  "org.slf4j" % "slf4j-log4j12" % "1.7.2"
)

// ivyXML := 
  // <dependency org="org.eclipse.jetty.orbit" name="javax.servlet" rev="2.5.0.v201103041518">
    // <artifact name="javax.servlet" type="orbit" ext="jar"/>
  // </dependency>

resolvers ++= Seq(
  "Akka Repository" at "http://repo.akka.io/releases/",
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases"
)

mainClass := Some("GlitchTest")

fork in run := true

javaOptions in run ++= Seq(
  // "-Dspark.speculation=true",
  "-Dspark.shuffle.sync=false",
  "-Dspark.streaming.useNewUnion=true",
  "-XX:+UseConcMarkSweepGC"
)


