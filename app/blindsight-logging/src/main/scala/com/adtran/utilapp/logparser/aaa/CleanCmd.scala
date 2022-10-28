package com.adtran.utilapp.logparser.aaa

import com.monovore.decline.*
import io.circe.Codec
import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.tersesystems.blindsight.{*, given}
import com.tersesystems.blindsight.DSL.{*, given}
import io.circe.*
import io.circe.syntax.*
import _root_.fs2.io.*

import java.nio.file.Path
import com.adtran.utilapp.logparser.aaa.codecs.ExternalCirceCodecs.{*, given}

/** Decline Command Line Parser. Throw in src path validation and we mkdir destDir if needed */

case class CleanCmd(srcDir: file.Path, destDir: file.Path) derives Codec.AsObject

case class StatsCmd(srcDir: file.Path, doPlot: Boolean) derives Codec.AsObject

object CleanCmd {
  val plotOpt: Opts[Boolean]  = Opts.flag("plot", help = "Output Files for Plotting w/ ??? ", "p").orFalse
  val srcDir: Opts[file.Path] = Opts.argument[Path]("srcDir").withDefault(Path.of("/Users/Shared/firefly-aaa/")).map(file.Path.fromNioPath)

  val outDir: Opts[file.Path] = Opts
    .option[Path]("out", "Directory to output files to", "o")
    .withDefault(Path.of("."))
    .map(file.Path.fromNioPath)

  val options: Opts[CleanCmd] = (srcDir, outDir).mapN(CleanCmd.apply)

  val normalizeCmd: Command[CleanCmd] = Command(name = "clean", "Gunzips, and combines logs in JSON format") {
    options
  }

  val histoCmd: Command[StatsCmd] = Command("stats", "Generates statistics from normalized logs made by clean command") {
    (srcDir, plotOpt).mapN(StatsCmd.apply)
  }

  val uberApp: Opts[Product] = Opts.subcommands(histoCmd, normalizeCmd)
//  given toStatement: ToStatement[ProgramCmd] = ToStatement { (v: ProgramCmd) =>
//    import com.tersesystems.blindsight.DSL._
//    Statement()
//      .withMessage("Program Cmd {}")
//      .withArguments(Arguments(v))
//  }
//
//  given ToArgument[com.adtran.utilapp.logparser.aaa.ProgramCmd] = ToArgument { cmd =>
//    import _root_.net.logstash.logback.argument.StructuredArguments.{given, *}
//
//    new Argument(raw("programArguments", cmd.asJson.spaces2))
//
//  }
}
