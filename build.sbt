import scala.io.Source

name := "dicoinerBot"

organization := "luger"

version := "0.1.0"

scalaVersion := "2.12.3"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

resolvers += Resolver.sonatypeRepo("snapshots")

val testDependencies = Seq(
    "org.scalactic" %% "scalactic" % "3.0.1",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "com.dimafeng" %% "testcontainers-scala" % "0.7.0" % "test"
)

val loggingDependencies = Seq(
  "tv.cntt" %% "slf4s-api" % "1.7.25",
  /*Fork of https://github.com/mattroberts297/slf4s to add support for Scala 2.12.*/
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

val botDependencies = Seq (
  "com.typesafe" % "config" % "1.3.1",
  "info.mukel" %% "telegrambot4s" % "3.0.9",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0",
  "org.scalaj" %% "scalaj-http" % "2.3.0"
)

libraryDependencies ++= botDependencies ++ loggingDependencies ++ testDependencies

enablePlugins(DockerPlugin, DockerComposePlugin)

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("java")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

variablesForSubstitution := Map("TELEGRAM_KEY" -> Source.fromFile("telegram.key").getLines().next)

dockerImageCreationTask := docker.value

test in docker := {}