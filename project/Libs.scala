object Libs {

  object Version {
    val logback                    = "1.4.1"
    val slf4s                      = "0.3.0"
    val blindsight                 = "1.5.2"
    val echopraxiaPlusScalaVersion = "1.1.1"
    val terseLogback               = "1.0.3"
    val jackson                    = "2.11.0"
    val catsV                      = "2.8.0"
    val catsEffectV                = "2.5.5"
    val circeV                     = "0.14.3"
    val circeOpticsV               = "0.14.1"
    val circeGenericExtrasV        = "0.14.2"
    val oslibV                     = "0.8.1"

    val slf4s_timoV = "1.7.30.2"
    val slf4sV      = "1.7.7"
    val slf4jV      = "1.7.1"

    // RUNTIME
    val logbackV = "1.4.1"
    val pprintV  = "0.8.0"

  }

  val TerseLogback = Seq(
    "com.tersesystems.logback" % "logback-tracing"           % Version.terseLogback,
    "com.tersesystems.logback" % "logback-uniqueid-appender" % Version.terseLogback,
    "com.tersesystems.logback" % "logback-honeycomb-client"  % Version.terseLogback,
    "com.tersesystems.logback" % "logback-sigar"             % Version.terseLogback
  )
  val Blindsight   = Seq(
    "com.tersesystems.blindsight" %% "blindsight-logstash"   % Version.blindsight,
    "com.tersesystems.blindsight" %% "blindsight-inspection" % Version.blindsight,
    "com.tersesystems.blindsight" %% "blindsight-api"        % Version.blindsight,
    "com.tersesystems.blindsight" %% "blindsight-generic"    % Version.blindsight
    // "com.tersesystems.blindsight"  % "blindsight-flow"           % "0.1.36",
    /*  "com.fasterxml.jackson.module" % "jackson-module-scala" % Version.jackson*/
  )
  //
  //  val echopraxia = Seq( // https://github.com/tersesystems/echopraxia-plusscala logging, succesor to blindsight
  //    ("com.tersesystems.echopraxia.plusscala" %% "logger"       % Version.echopraxiaPlusScalaVersion).cross(CrossVersion.for3Use2_13),
  //    // "com.tersesystems.echopraxia.plusscala" %% "async"    % Version.echopraxiaPlusScalaVersion,
  //    ("com.tersesystems.echopraxia.plusscala" %% "nameof"       % Version.echopraxiaPlusScalaVersion).cross(CrossVersion.for3Use2_13),
  //    ("com.tersesystems.echopraxia.plusscala" %% "trace-logger" % Version.echopraxiaPlusScalaVersion).cross(CrossVersion.for3Use2_13),
  //    ("com.tersesystems.echopraxia.plusscala" %% "flow-logger"  % Version.echopraxiaPlusScalaVersion).cross(CrossVersion.for3Use2_13),
  //    ("com.tersesystems.echopraxia.plusscala" %% "diff"         % Version.echopraxiaPlusScalaVersion).cross(CrossVersion.for3Use2_13),
  //    ("com.tersesystems.echopraxia.plusscala" %% "generic"      % Version.echopraxiaPlusScalaVersion).cross(CrossVersion.for3Use2_13),
  //    ("com.tersesystems.echopraxia.plusscala" %% "api"          % Version.echopraxiaPlusScalaVersion).cross(CrossVersion.for3Use2_13),
  //    "com.tersesystems.echopraxia"             % "logstash"     % "2.2.2"
  //  )

  lazy val PPrint = Seq("com.lihaoyi" %% "pprint" % Version.pprintV % Test)

  lazy val Logging = Seq(
    // "org.slf4j" % "slf4j-api" % slf4jV, -- Abandoned, timo-schmid instead for all up to 3.0
    "ch.timo-schmid" %% "slf4s-api"       % Version.slf4s_timoV,
    "ch.qos.logback"  % "logback-classic" % Version.logback % Provided
  )

  /** Circe Scala 3 very different set */
  lazy val Circe = Seq(
    "io.circe" %% "circe-core"    % Version.circeV,
    "io.circe" %% "circe-generic" % Version.circeV,
    "io.circe" %% "circe-parser"  % Version.circeV,
    "io.circe" %% "circe-literal" % Version.circeV
  )

  // lazy val JodaTime = Seq("joda-time" % "joda-time" % jodaTimeV)
  lazy val Cats =
    Seq("org.typelevel" %% "cats-core" % Version.catsV, "org.typelevel" %% "cats-effect" % Version.catsEffectV)

}
