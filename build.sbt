organization := "eu.inn"

name := "fluentd-scala"

version := "0.1.2"

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

publishMavenStyle := true

publishArtifact in Test := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

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
