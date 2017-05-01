name := "amrotron"

version := "0.1-SNAPSHOT"

organization := "com.oschrenk"

scalaVersion := "2.12.2"

test in assembly := {}

mainClass in assembly := Some("Cli")

libraryDependencies ++= {
  val kantanV = "0.1.18"
  Seq(
    "com.nrinaudo" %% "kantan.csv" % kantanV,
    "com.nrinaudo" %% "kantan.csv-java8" % kantanV,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.github.scopt" %% "scopt" % "3.5.0",
    "org.scalatest" %% "scalatest" % "3.0.3" % "test"
  )
}
