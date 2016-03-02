credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  Option(System.getenv().get("SONATYPE_USERNAME")).getOrElse("NOT FOUND!!!"),
  Option(System.getenv().get("SONATYPE_USERNAME")).getOrElse("NOT FOUND!!!")
)
