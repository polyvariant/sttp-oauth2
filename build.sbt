inThisBuild(
  List(
    organization := "com.ocadotechnology",
    homepage := Some(url("https://github.com/ocadotechnology/sttp-oauth2")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "majk-p",
        "Michał Pawlik",
        "michal.pawlik@ocado.com",
        url("https://michalp.net")
      ),
      Developer(
        "kubukoz",
        "Jakub Kozłowski",
        "j.kozlowski@ocado.com",
        url("https://github.com/kubukoz")
      ),
      Developer(
        "tplaskowski",
        "Tomek Pląskowski",
        "t.plaskowski@ocado.com",
        url("https://github.com/tplaskowski")
      )
    ),
    versionScheme := Some("early-semver")
  )
)

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x.cross(CrossVersion.full))

val Scala212 = "2.12.12"
val Scala213 = "2.13.4"

val GraalVM11 = "graalvm-ce-java11@20.3.0"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala212, Scala213)
ThisBuild / githubWorkflowJavaVersions := Seq(GraalVM11)
ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("test", "mimaReportBinaryIssues"))) // NOTE those run separately for every ScalaVersion in `crossScalaVersions`

//sbt-ci-release settings
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublishPreamble := Seq(WorkflowStep.Use("olafurpg", "setup-gpg", "v3"))
ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowEnv ++= List("PGP_PASSPHRASE", "PGP_SECRET", "SONATYPE_PASSWORD", "SONATYPE_USERNAME").map { envKey =>
  envKey -> s"$${{ secrets.$envKey }}"
}.toMap


val Versions = new {
  val sttp = "2.0.9"
  val circe = "0.13.0"
}

val commonDependencies = {

  val cats = Seq(
    "org.typelevel" %% "cats-tagless-macros" % "0.11"
  )

  val circe = Seq(
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-literal",
    "io.circe" %% "circe-refined"
  ).map(_ % Versions.circe)

  val plugins = Seq(
    compilerPlugin("org.typelevel" % "kind-projector" % "0.11.1" cross CrossVersion.full)
  )

  val sttp = Seq(
    "com.softwaremill.sttp.client" %% "core" % Versions.sttp,
    "com.softwaremill.sttp.client" %% "circe" % Versions.sttp
  )

  val refined = Seq(
    "eu.timepit" %% "refined" % "0.9.14",
    "eu.timepit" %% "refined-cats" % "0.9.14"
  )

  cats ++ circe ++ sttp ++ refined ++ plugins
}

val oauth2Dependencies = {
  val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % "3.1.2"
  ).map(_ % Test)

  commonDependencies ++ testDependencies
}

val mimaSettings = mimaPreviousArtifacts := Set(
  // organization.value %% name.value % "1.0.0"
)

lazy val oauth2 = project.settings(
  name := "sttp-oauth2",
  libraryDependencies ++= oauth2Dependencies,
  mimaSettings
)

val root = project
  .in(file("."))
  .settings(
    skip in publish := true,
    mimaPreviousArtifacts := Set.empty
  )
  .aggregate(oauth2)