inThisBuild(
  List(
    organization := "com.ocadotechnology",
    homepage := Some(url("https://github.com/ocadotechnology/sttp-oauth2")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
    ),
    sources in (Compile, doc) := Seq()
  )
)

val Scala212 = "2.12.12"
val Scala213 = "2.13.4"

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x.cross(CrossVersion.full))

val compilerPlugins = List(
  crossPlugin("org.typelevel" % "kind-projector" % "0.11.2"),
  crossPlugin("com.github.cb372" % "scala-typed-holes" % "0.1.5"),
  crossPlugin("com.kubukoz" % "better-tostring" % "0.2.4"),
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

val GraalVM11 = "graalvm-ce-java11@20.3.0"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala212, Scala213)
ThisBuild / githubWorkflowJavaVersions := Seq(GraalVM11)
ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("test", "mimaReportBinaryIssues")))

//sbt-ci-release settings
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublishPreamble := Seq(WorkflowStep.Use("olafurpg", "setup-gpg", "v3"))
ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowEnv ++= List("PGP_PASSPHRASE", "PGP_SECRET", "SONATYPE_PASSWORD", "SONATYPE_USERNAME").map { envKey =>
  envKey -> s"$${{ secrets.$envKey }}"
}.toMap

val mimaSettings = mimaPreviousArtifacts := Set(
  // organization.value %% name.value % "1.0.0"
)

val oauth2 = project
  .settings(
    name := "sttp-oauth2",
    mimaSettings
  )

val root = project
  .in(file("."))
  .settings(
    skip in publish := true,
    mimaPreviousArtifacts := Set.empty
  )
  .aggregate(oauth2)
