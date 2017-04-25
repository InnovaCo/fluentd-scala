organization := "eu.inn"

name := "fluentd-scala"

version := "0.2"

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.11.11", "2.12.2")

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-language:implicitConversions",
  "-feature",
  "-deprecation",
  "-unchecked",
  "-optimise",
  "-target:jvm-1.8",
  "-encoding", "UTF-8"
)

javacOptions ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-encoding", "UTF-8",
  "-Xlint:unchecked",
  "-Xlint:deprecation"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "org.msgpack" % "msgpack-scala_2.11" % "0.6.11",
  "ch.qos.logback" % "logback-classic" % "1.2.2"
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

pgpSecretRing := file("./travis/gpg-private.asc.gpg")

pgpPublicRing := file("./travis/gpg-public.asc.gpg")

usePgpKeyHex("1FC91057C33D1A33")

pgpPassphrase := Option(System.getenv().get("oss_gpg_passphrase")).map(_.toCharArray)

credentials += Credentials("Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  System.getenv().get("sonatype_username"),
  System.getenv().get("sonatype_password"))
