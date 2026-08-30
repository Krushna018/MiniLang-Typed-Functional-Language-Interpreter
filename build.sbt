ThisBuild / scalaVersion := "3.3.4"
ThisBuild / organization := "edu.minilang"
ThisBuild / version := "0.1.0"

Compile / mainClass := Some("minilang.Main")

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked"
)
