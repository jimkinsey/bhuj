publishMavenStyle := true

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := <url>https://github.com/jimkinsey/bhuj</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:jimkinsey/bhuj.git</url>
    <connection>scm:git:git@github.com:jimkinsey/bhuj.git</connection>
  </scm>
  <developers>
    <developer>
      <id>jimkinsey</id>
      <name>Jim Kinsey</name>
      <url>http://github.com/jimkinsey</url>
    </developer>
  </developers>