name := "dicoinerBot"

organization := "me.luger"

version := "0.1.0"

scalaVersion := "2.12.3"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

resolvers += Resolver.sonatypeRepo("snapshots")

val testDependencies = Seq(
    "org.scalactic" %% "scalactic" % "3.0.1",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

val loggingDependencies = Seq(
  "tv.cntt" %% "slf4s-api" % "1.7.25",
  /*Fork of https://github.com/mattroberts297/slf4s to add support for Scala 2.12.*/
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

val botDependencies = Seq (
  "com.typesafe" % "config" % "1.3.1",
  "info.mukel" %% "telegrambot4s" % "3.0.9",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"
)

libraryDependencies ++= botDependencies ++ loggingDependencies ++ testDependencies

