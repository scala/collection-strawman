val commonSettings = Seq(
  scalaVersion := "2.13.0-M4-pre-20d3c21",
  organization := "ch.epfl.scala",
  version := "0.10.0-SNAPSHOT",
  scalacOptions ++= Seq("-deprecation", "-feature", "-opt-warnings", "-unchecked", "-language:higherKinds"/*, "-opt:l:classpath"*/),
  scalacOptions in (Compile, doc) ++= Seq("-implicits", "-groups"),
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v", "-s", "-a"),
  fork in Test := true,
  parallelExecution in Test := false,
  homepage := Some(url("https://github.com/scala/collection-strawman")),
  licenses := Seq("BSD 3-clause" -> url("http://opensource.org/licenses/BSD-3-Clause")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/scala/collection-strawman"),
      "scm:git:git@github.com:scala/collection-strawman.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer><id>ichoran</id><name>Rex Kerr</name></developer>
      <developer><id>odersky</id><name>Martin Odersky</name></developer>
      <developer><id>pathikrit</id><name>Pathikrit Bhowmick</name></developer>
      <developer><id>julienrf</id><name>Julien Richard-Foy</name></developer>
      <developer><id>szeiger</id><name>Stefan Zeiger</name></developer>
      <developer><id>msteindorfer</id><name>Michael J. Steindorfer</name></developer>
    </developers>,
  // For publishing snapshots
  credentials ++= (
    for {
      username <- sys.env.get("SONATYPE_USERNAME")
      password <- sys.env.get("SONATYPE_PASSWORD")
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)
  ).toList
)

// TODO Make it a cross project when Scala.js is released for 2.13.0-M4
val `collections-contrib` =
  project.in(file("collections-contrib"))
    .settings(
      commonSettings,
      libraryDependencies ++= Seq(
        "junit"            % "junit"           % "4.11",
        "com.novocode"     % "junit-interface" % "0.11"   % Test,
        "org.openjdk.jol"  % "jol-core"        % "0.5"
      ),
      testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")
    )
