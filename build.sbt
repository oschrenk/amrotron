name := "sparkplug"

organization := "com.oschrenk"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.2"

libraryDependencies ++= {
  val kantanV = "0.1.18"
  Seq(
    "com.nrinaudo" %% "kantan.csv" % kantanV,
    "com.nrinaudo" %% "kantan.csv-java8" % kantanV,
    "com.nrinaudo" %% "kantan.csv-generic" % kantanV,
    "org.scalatest" %% "scalatest" % "3.0.3" % "test"
   )
}
