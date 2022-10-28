import MyCompileOptions._

inThisBuild(
  Seq(
    organization      := "com.adtran",
    organizationName  := "adtran",
    startYear         := Some(2022),
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),
    homepage          := Some(url("https://github.com/odenzo/")),
    developers        := List(Developer("odenzo", "odenzo", "mail@blackhole.com", url("https://github.com/odenzo"))),
    scalaVersion      := "3.2.0",
    scalacOptions ++= optsV3_0 ++ warningsV3_0 ++ lintersV3_0,
    semanticdbEnabled := false,
    bspEnabled        := false,
   
  )
)

Test / fork              := true
Test / parallelExecution := false
Test / logBuffered       := false

val root = (project in file("."))
  .settings(name := "BlindsightLogging")
  .aggregate(blindsight)


lazy val blindsight = (project in file("app/blindsight-logging"))
  .settings(
    name := "BlindSight",
     mainClass         := Some("com.adtran.utilapp.logparser.aaa.CommandLine")
    libraryDependencies ++= Libs.cats ++ Libs.catsParse ++ Libs.kittens ++ Libs.stdLibs ++ Libs.circe,
    libraryDependencies ++= Libs.decline ++ Libs.ducktape ++ Libs.fs2,
    libraryDependencies ++= Libs.comcastNetworks ++ Libs.http4s,
    libraryDependencies ++= Libs.testingMUnit
  )
