////noinspection TypeAnnotation
//object Libs {
//
//  lazy val stdLibs = Seq(
//    "com.lihaoyi"   %% "pprint"           % Version.pprint,
//    "com.lihaoyi"   %% "os-lib"           % Version.osLib,
//    "org.typelevel" %% "case-insensitive" % Version.caseInsensitive,
//    "org.typelevel" %% "literally"        % Version.literally
//  )
//
//  // These are my standard stack and are all ScalaJS enabled.
//  lazy val cats = Seq("org.typelevel" %% "cats-core" % Version.cats, "org.typelevel" %% "cats-effect" % Version.catsEffect)
//
//  /** Currently this is only for the binary serialization */
//  // val libs_html = Seq("com.lihaoyi" %% "scalatags" % Version.scalaTags, "com.github.japgolly.scalacss" %% "core" % Version.scalaCss)
//
//  // As of 0.14.3 Circe Suite Partually migrated to scala 3
//  lazy val circe = Seq(
//    "io.circe" %% "circe-core"            % Version.circe,
//    // "io.circe" %% "circe-jackson211" % "0.14.0"
//    "io.circe" %% "circe-generic"         % Version.circe,
//    // "io.circe" %% "circe-extras"          % Version.circe,
//    "io.circe" %% "circe-parser"          % Version.circe,
//    "io.circe" %% "circe-pointer"         % Version.circe,
//    "io.circe" %% "circe-pointer-literal" % Version.circe,
//    // "io.circe" %% "circe-optics"  % circeOpticsVersion
//    "io.circe" %% "circe-literal"         % Version.circe,
//    "io.circe" %% "circe-scodec"          % Version.circe,
//    "io.circe" %% "circe-fs2"             % Version.circeFS2
//  )
//
//
//
//  lazy val all = catsParse ++ scodec ++ testingMUnit ++ circe ++ fs2 ++ cats ++ stdLibs ++ monocle ++ decline ++ blindsight ++ pureconfig
//
//}
