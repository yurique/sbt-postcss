sbtPlugin := true

organization := "ws.kotonoha.sbt"

name := "sbt-postcss"

version := "0.1.0-SNAPSHOT"

crossSbtVersions := Vector("0.13.16", "1.0.0", "1.1.0")

resolvers ++= Seq(
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

addSbtPlugin("com.typesafe.sbt" %% "sbt-js-engine" % "1.2.2")

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

publishMavenStyle := false

publishTo := {
  if (isSnapshot.value) Some(Classpaths.sbtPluginSnapshots)
  else Some(Classpaths.sbtPluginReleases)
}

scriptedSettings

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value, "-Dproject.version=" + version.value)
}

scriptedBufferLog := false
