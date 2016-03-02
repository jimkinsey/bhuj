name := "bhuj"

organization := "com.github.jimkinsey"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings", "-Xlint")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "org.mockito" % "mockito-core" % "1.8.5"  % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.5" % "test"
