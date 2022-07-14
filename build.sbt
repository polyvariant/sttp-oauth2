import sbtghactions.UseRef

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
        "tplaskowski",
        "Tomek Pląskowski",
        "t.plaskowski@ocado.com",
        url("https://github.com/tplaskowski")
      ),
      Developer(
        "matwojcik",
        "Mateusz Wójcik",
        "mateusz.wojcik@ocado.com",
        url("https://github.com/matwojcik")
      )
    ),
    versionScheme := Some("early-semver")
  )
)

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x.cross(CrossVersion.full))

val Scala212 = "2.12.16"
val Scala213 = "2.13.8"
val Scala3 = "3.1.3"

val GraalVM11 = "graalvm-ce-java11@20.3.0"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala212, Scala213, Scala3)
ThisBuild / githubWorkflowJavaVersions := Seq(GraalVM11)
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("test", "docs/mdoc", "mimaReportBinaryIssues"))
) // NOTE those run separately for every ScalaVersion in `crossScalaVersions`

//sbt-ci-release settings
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  // the default is master - https://github.com/djspiewak/sbt-github-actions/issues/41
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowPublishPreamble := Seq(WorkflowStep.Use(UseRef.Public("olafurpg", "setup-gpg", "v3")))
ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowEnv ++= List("PGP_PASSPHRASE", "PGP_SECRET", "SONATYPE_PASSWORD", "SONATYPE_USERNAME").map { envKey =>
  envKey -> s"$${{ secrets.$envKey }}"
}.toMap

val Versions = new {
  val catsCore = "2.8.0"
  val catsEffect = "3.3.13"
  val catsEffect2 = "2.5.5"
  val circe = "0.14.2"
  val monix = "3.4.1"
  val scalaTest = "3.2.12"
  val sttp = "3.3.18"
  val refined = "0.9.29"
}

def compilerPlugins =
  libraryDependencies ++= (if (scalaVersion.value.startsWith("3")) Seq()
                           else Seq(compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")))

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % Versions.scalaTest
).map(_ % Test)

val mimaSettings =
  mimaPreviousArtifacts := {
    val currentVersion = version.value
    lazy val onlyPatchChanged =
      previousStableVersion.value.flatMap(CrossVersion.partialVersion) == CrossVersion.partialVersion(currentVersion)
    lazy val isRcOrMilestone = currentVersion.contains("-M") || currentVersion.contains("-RC")
    if (onlyPatchChanged && !isRcOrMilestone) {
      previousStableVersion.value.map(organization.value %% moduleName.value % _).toSet
    } else {
      Set.empty
    }
  }

lazy val oauth2 = project.settings(
  name := "sttp-oauth2",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % Versions.catsCore,
    "io.circe" %% "circe-parser" % Versions.circe,
    "io.circe" %% "circe-core" % Versions.circe,
    "io.circe" %% "circe-refined" % Versions.circe,
    "com.softwaremill.sttp.client3" %% "core" % Versions.sttp,
    "com.softwaremill.sttp.client3" %% "circe" % Versions.sttp,
    "eu.timepit" %% "refined" % Versions.refined
  ) ++ testDependencies,
  mimaSettings,
  compilerPlugins
)

lazy val docs = project
  .in(file("mdoc")) // important: it must not be docs/
  .settings(
    mdocVariables := Map(
      "VERSION" -> { if (isSnapshot.value) previousStableVersion.value.get else version.value }
    )
  )
  .dependsOn(oauth2)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)

lazy val `oauth2-cache` = project
  .settings(
    name := "sttp-oauth2-cache",
    mimaSettings,
    compilerPlugins
  )
  .dependsOn(oauth2)

lazy val `oauth2-cache-cats` = project
  .settings(
    name := "sttp-oauth2-cache-cats",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect-kernel" % Versions.catsEffect,
      "org.typelevel" %% "cats-effect-std" % Versions.catsEffect,
      "org.typelevel" %% "cats-effect" % Versions.catsEffect % Test,
      "org.typelevel" %% "cats-effect-testkit" % Versions.catsEffect % Test
    ) ++ testDependencies,
    mimaPreviousArtifacts := Set.empty,
    compilerPlugins
  )
  .dependsOn(`oauth2-cache`)

lazy val `oauth2-cache-ce2` = project
  .settings(
    name := "sttp-oauth2-cache-ce2",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % Versions.catsEffect2,
      "org.typelevel" %% "cats-effect-laws" % Versions.catsEffect2 % Test
    ) ++ testDependencies,
    mimaSettings,
    compilerPlugins
  )
  .dependsOn(`oauth2-cache`)

lazy val `oauth2-cache-future` = project
  .settings(
    name := "sttp-oauth2-cache-future",
    libraryDependencies ++= Seq(
      "io.monix" %% "monix-execution" % Versions.monix
    ) ++ testDependencies,
    mimaSettings,
    compilerPlugins
  )
  .dependsOn(`oauth2-cache`)

val root = project
  .in(file("."))
  .settings(
    publish / skip := true,
    mimaPreviousArtifacts := Set.empty
  )
  // after adding a module remember to regenerate ci.yml using `sbt githubWorkflowGenerate`
  .aggregate(oauth2, `oauth2-cache`, `oauth2-cache-cats`, `oauth2-cache-ce2`, `oauth2-cache-future`)
