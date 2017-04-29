name := "sparkplug"

organization := "com.oschrenk"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.2"

libraryDependencies ++= {
  Seq(
      "com.prowidesoftware" % "pw-swift-core" % "SRU2016-7.8.5",

      "org.scalatest" %% "scalatest" % "3.0.3" % "test"
   )
}
