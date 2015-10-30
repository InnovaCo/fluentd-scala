import SonatypeKeys._

sonatypeSettings

organization := "eu.inn"

name := "fluentd-scala"

version := "0.1.15"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.10.4", "2.11.6")

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-feature",
  "-deprecation",
  "-optimise",
  "-encoding", "utf8"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "org.msgpack" %% "msgpack-scala" % "0.6.11",
  "ch.qos.logback" % "logback-classic" % "1.1.2"
)

pomExtra := {
  <url>https://github.com/InnovaCo/fluentd-scala</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://opensource.org/licenses/BSD-3-Clause</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:InnovaCo/fluentd-scala.git</url>
    <connection>scm:git:git@github.com:InnovaCo/fluentd-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>InnovaCo</id>
      <name>Innova Co S.a r.l</name>
      <url>https://github.com/InnovaCo</url>
    </developer>
    <developer>
      <id>kulikov</id>
      <name>Dmitry Kulikov</name>
      <url>https://github.com/kulikov</url>
    </developer>
  </developers>
}
