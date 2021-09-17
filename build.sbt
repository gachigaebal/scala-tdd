val V = new {
  val Scala      = "3.1.0-RC2"
  val ScalaGroup = "3.1"

  val hedgehog        = "0.7.0"
  val organiseImports = "0.5.0"
}

val Dependencies = new {

  lazy val domain = Seq(
    libraryDependencies ++= Seq(
    )
  )

  lazy val tests = Def.settings(
    libraryDependencies ++= Seq(
      "qa.hedgehog" %% "hedgehog-minitest" % V.hedgehog % Test,
    ),
    testFrameworks += TestFramework("minitest.runner.Framework"),
  )
}

ThisBuild / organization := "gachigaebal"
ThisBuild / version      := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := V.Scala
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % V.organiseImports
ThisBuild / semanticdbEnabled := true

ThisBuild / scalacOptions ++= Seq(
  "-source:future", // enable future language features
  "-deprecation",   // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
)

ThisBuild / Compile / console / scalacOptions ~= (_.filterNot(
  Set(
    "-Ywarn-unused:imports",
    "-Xfatal-warnings",
  )
))

lazy val root =
  (project in file(".")).aggregate(domain)

lazy val domain = (project in file("modules/domain"))
  .settings(Dependencies.domain)
  .settings(Dependencies.tests)
  .settings(
    name        := "scala-tdd-domain",
    Test / fork := true,
  )
