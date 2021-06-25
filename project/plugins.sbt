addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.20")
addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.12.0")
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.7")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.9.2")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.21")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
libraryDependencies ++= Seq(
  "io.circe" %% "circe-parser" % "0.14.1",
  "io.circe" %% "circe-optics" % "0.14.1"
)
