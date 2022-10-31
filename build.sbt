scalaVersion      := "2.13.8"
scalacOptions ++= ScalaOpts.V213
semanticdbEnabled := false
bspEnabled        := false

Test / fork              := true
Test / parallelExecution := false
Test / logBuffered       := false

val root = (project in file("."))
  .settings(name := "BlindsightLogging")
  .aggregate(blindsight)

lazy val blindsight = (project in file("app/blindsight-logging"))
  .settings(
    name      := "BlindSight",
    mainClass := Some("com.adtran.utilapp.logparser.aaa.CommandLine"),
    libraryDependencies ++= Libs.Cats ++ Libs.PPrint ++ Libs.Circe,
    libraryDependencies ++= Libs.Blindsight ++ Libs.TerseLogback
  )
