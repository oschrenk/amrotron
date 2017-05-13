name := "amrotron"

version := "0.1-SNAPSHOT"

organization := "com.oschrenk"

scalaVersion := "2.12.2"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",      // yes, this is 2 args
  "-unchecked",              // provide more info about type erasure
  "-deprecation",            // warn about deprecated usaage
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xlint",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",    // warn when values aer discarded
  "-Xfuture",
  "-Ywarn-unused-import"     // 2.11+ only
)
test in assembly := {}

mainClass in assembly := Some("Cli")

libraryDependencies ++= {
  val kantanV = "0.1.18"
  Seq(
    "com.nrinaudo" %% "kantan.csv" % kantanV,
    "com.nrinaudo" %% "kantan.csv-java8" % kantanV,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.parboiled" %% "parboiled" % "2.1.4",
    "com.github.scopt" %% "scopt" % "3.5.0",
    "org.scalatest" %% "scalatest" % "3.0.3" % "test"
  )
}
