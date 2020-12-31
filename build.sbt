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
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("test", "mimaReportBinaryIssues"))
) // NOTE those run separately for every ScalaVersion in `crossScalaVersions`

//sbt-ci-release settings
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  // the default is master - https://github.com/djspiewak/sbt-github-actions/issues/41
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowPublishPreamble := Seq(WorkflowStep.Use("olafurpg", "setup-gpg", "v3"))
ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowEnv ++= List("PGP_PASSPHRASE", "PGP_SECRET", "SONATYPE_PASSWORD", "SONATYPE_USERNAME").map { envKey =>
  envKey -> s"$${{ secrets.$envKey }}"
}.toMap

val Versions = new {
  val catsCore = "2.3.1"
  val catsEffect = "2.3.1"
  val circe = "0.13.0"
  val kindProjector = "0.11.2"
  val scalaTest = "3.2.3"
  val sttp = "2.0.9"
  val refined = "0.9.19"
}

val commonDependencies = {

  val cats = Seq(
    "org.typelevel" %% "cats-core" % Versions.catsCore,
    "org.typelevel" %% "cats-effect" % Versions.catsEffect
  )

  val circe = Seq(
    "io.circe" %% "circe-parser" % Versions.circe,
    "io.circe" %% "circe-core" % Versions.circe,
    "io.circe" %% "circe-refined" % Versions.circe
  )

  val plugins = Seq(
    compilerPlugin("org.typelevel" % "kind-projector" % Versions.kindProjector cross CrossVersion.full)
  )

  val sttp = Seq(
    "com.softwaremill.sttp.client" %% "core" % Versions.sttp,
    "com.softwaremill.sttp.client" %% "circe" % Versions.sttp
  )

  val refined = Seq(
    "eu.timepit" %% "refined" % Versions.refined,
    "eu.timepit" %% "refined-cats" % Versions.refined
  )

  cats ++ circe ++ sttp ++ refined ++ plugins
}

val oauth2Dependencies = {
  val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % Versions.scalaTest,
    "io.circe" %% "circe-literal" % Versions.circe
  ).map(_ % Test)

  commonDependencies ++ testDependencies
}

val mimaSettings = mimaPreviousArtifacts := Set(
  organization.value %% name.value % "0.1.0"
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
