import AssemblyKeys._

name := "AkkaSandbox"

version := "0.3"

scalaVersion := "2.9.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.2"

libraryDependencies += "fr.janalyse"   %% "janalyse-ssh" % "0.8.0" % "compile"


resolvers += "JAnalyse Repository" at "http://www.janalyse.fr/repository/"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"


seq(assemblySettings: _*)

mainClass in assembly := Some("dummy.DummySSH")

jarName in assembly := "dummy.jar"
