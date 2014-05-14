organization := "eu.inn"

name := "fluentd-scala"

version := "0.1.0"

scalaVersion := "2.10.4"

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-feature",
  "-deprecation",
  "-optimise",
  "-encoding", "utf8"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.0",
  "org.msgpack" %% "msgpack-scala" % "0.6.8",
  "ch.qos.logback" % "logback-classic" % "1.1.2"
)
