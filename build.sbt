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

val Scala212 = "2.12.17"
val Scala213 = "2.13.11"
val Scala3 = "3.2.2"

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
  val catsEffect = "3.3.14"
  val catsEffect2 = "2.5.5"
  val circe = "0.14.5"
  val jsoniter = "2.21.4"
  val monix = "3.4.1"
  val scalaTest = "3.2.16"
  val sttp = "3.3.18"
  val refined = "0.10.3"
  val scalaCache = "1.0.0-M6"
}

def compilerPlugins =
  libraryDependencies ++= (if (scalaVersion.value.startsWith("3")) Seq()
                           else Seq(compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")))

val mimaSettings =
  // revert the commit that made this change after releasing a new version
  // mimaPreviousArtifacts := {
  //  val currentVersion = version.value
  //  lazy val onlyPatchChanged =
  //    previousStableVersion.value.flatMap(CrossVersion.partialVersion) == CrossVersion.partialVersion(currentVersion)
  //  lazy val isRcOrMilestone = currentVersion.contains("-M") || currentVersion.contains("-RC")
  //  if (onlyPatchChanged && !isRcOrMilestone) {
  //    previousStableVersion.value.map(organization.value %% moduleName.value % _).toSet
  //  } else {
  //    Set.empty
  //  }
  // }
  mimaPreviousArtifacts := Set.empty

// Workaround for https://github.com/typelevel/sbt-tpolecat/issues/102
val jsSettings = scalacOptions ++= (if (scalaVersion.value.startsWith("3")) Seq("-scalajs") else Seq())

lazy val oauth2 = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .settings(
    name := "sttp-oauth2",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %%% "core" % Versions.sttp,
      "org.typelevel" %%% "cats-core" % Versions.catsCore,
      "eu.timepit" %%% "refined" % Versions.refined,
      "org.scalatest" %%% "scalatest" % Versions.scalaTest % Test
    ),
    mimaSettings,
    compilerPlugins
  )
  .jsSettings(
    libraryDependencies ++= Seq("org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0"),
    jsSettings
  )

lazy val `oauth2-circe` = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .in(file("oauth2-circe"))
  .settings(
    name := "sttp-oauth2-circe",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-parser" % Versions.circe,
      "io.circe" %%% "circe-core" % Versions.circe,
      "io.circe" %%% "circe-refined" % Versions.circe
    ),
    mimaSettings,
    compilerPlugins
  )
  .jsSettings(
    jsSettings
  )
  .dependsOn(oauth2 % "compile->compile;test->test")

lazy val `oauth2-jsoniter` = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .in(file("oauth2-jsoniter"))
  .settings(
    name := "sttp-oauth2-jsoniter",
    libraryDependencies ++= Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core" % Versions.jsoniter,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Versions.jsoniter % "compile-internal"
    ),
    mimaSettings,
    compilerPlugins,
    scalacOptions ++= Seq("-Wconf:cat=deprecation:info") // jsoniter-scala macro-generated code uses deprecated methods
  )
  .jsSettings(
    jsSettings
  )
  .dependsOn(oauth2 % "compile->compile;test->test")

lazy val docs = project
  .in(file("mdoc")) // important: it must not be docs/
  .settings(
    mdocVariables := Map(
      "VERSION" -> { if (isSnapshot.value) previousStableVersion.value.get else version.value }
    )
  )
  .dependsOn(oauth2.jvm)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)

lazy val `oauth2-cache` = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .settings(
    name := "sttp-oauth2-cache",
    mimaSettings,
    compilerPlugins
  )
  .jsSettings(jsSettings)
  .dependsOn(oauth2)

// oauth2-cache-scalacache doesn't have JS support because scalacache doesn't compile for js https://github.com/cb372/scalacache/issues/354#issuecomment-913024231
lazy val `oauth2-cache-scalacache` = project
  .settings(
    name := "sttp-oauth2-cache-scalacache",
    libraryDependencies ++= Seq(
      "com.github.cb372" %%% "scalacache-core" % Versions.scalaCache,
      "com.github.cb372" %% "scalacache-caffeine" % Versions.scalaCache % Test,
      "org.typelevel" %%% "cats-effect-kernel" % Versions.catsEffect,
      "org.typelevel" %%% "cats-effect-std" % Versions.catsEffect,
      "org.typelevel" %%% "cats-effect" % Versions.catsEffect % Test,
      "org.typelevel" %%% "cats-effect-testkit" % Versions.catsEffect % Test,
      "org.scalatest" %%% "scalatest" % Versions.scalaTest % Test
    ),
    mimaPreviousArtifacts := Set.empty,
    compilerPlugins
  )
  .dependsOn(`oauth2-cache`.jvm)

// oauth2-cache-cats doesn't have JS support because cats effect does not provide realTimeInstant on JS
lazy val `oauth2-cache-cats` = project
  .settings(
    name := "sttp-oauth2-cache-cats",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-kernel" % Versions.catsEffect,
      "org.typelevel" %%% "cats-effect-std" % Versions.catsEffect,
      "org.typelevel" %%% "cats-effect" % Versions.catsEffect % Test,
      "org.typelevel" %%% "cats-effect-testkit" % Versions.catsEffect % Test,
      "org.scalatest" %%% "scalatest" % Versions.scalaTest % Test
    ),
    mimaSettings,
    compilerPlugins
  )
  .dependsOn(`oauth2-cache`.jvm)

// oauth2-cache-ce2 doesn't have JS support because cats effect does not provide realTimeInstant on JS
lazy val `oauth2-cache-ce2` = project
  .settings(
    name := "sttp-oauth2-cache-ce2",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % Versions.catsEffect2,
      "org.typelevel" %% "cats-effect-laws" % Versions.catsEffect2 % Test,
      "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
    ),
    mimaSettings,
    compilerPlugins
  )
  .dependsOn(`oauth2-cache`.jvm)

lazy val `oauth2-cache-future` = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .settings(
    name := "sttp-oauth2-cache-future",
    libraryDependencies ++= Seq(
      "io.monix" %%% "monix-execution" % Versions.monix,
      "org.scalatest" %%% "scalatest" % Versions.scalaTest % Test
    ),
    mimaSettings,
    compilerPlugins
  )
  .jsSettings(jsSettings)
  .dependsOn(`oauth2-cache`)

val root = project
  .in(file("."))
  .settings(
    publish / skip := true,
    mimaPreviousArtifacts := Set.empty
  )
  // after adding a module remember to regenerate ci.yml using `sbt githubWorkflowGenerate`
  .aggregate(
    oauth2.jvm,
    oauth2.js,
    `oauth2-cache`.jvm,
    `oauth2-cache`.js,
    `oauth2-cache-cats`,
    `oauth2-cache-ce2`,
    `oauth2-cache-future`.jvm,
    `oauth2-cache-future`.js,
    `oauth2-cache-scalacache`,
    `oauth2-circe`.jvm,
    `oauth2-circe`.js,
    `oauth2-jsoniter`.jvm,
    `oauth2-jsoniter`.js
  )
