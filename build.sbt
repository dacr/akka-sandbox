import AssemblyKeys._

name := "AkkaSandbox"

version := "0.1"

scalaVersion := "2.9.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0-RC2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"


seq(assemblySettings: _*)

mainClass in assembly := Some("dummy.Dummy")

jarName in assembly := "dummy.jar"
