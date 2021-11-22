name := "kanban-web"

scalaVersion := "2.13.7"

val zioVersion = "1.0.11"

libraryDependencies ++= Seq(
    "dev.zio" %% "zio" % zioVersion,
    "dev.zio" %% "zio-test" % zioVersion % Test,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")