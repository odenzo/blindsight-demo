package com.adtran.utilapp.logparser.aaa

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.adtran.utilapp.logparser.aaa.CleanCmd.{options, uberApp}
import com.adtran.utilapp.logparser.aaa.codecs.BlindsightCodecs.{*, given}
import com.adtran.utilapp.logparser.aaa.codecs.ExternalCirceCodecs.given
import com.monovore.decline.*
import com.monovore.decline.effect.*
import com.tersesystems.blindsight.ArgumentEnrichment.*
import com.tersesystems.blindsight.DSL.{*, given}
import com.tersesystems.blindsight.{*, given}
import com.tersesystems.logback.sigar.*
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.given
import net.logstash.logback.argument.StructuredArguments.*

object CommandLineMain extends CommandIOApp("aaalogs", header = "Parse AAA Logs for API Usage", true, "0.0.2") {
  private val logger                    = org.log4s.getLogger // LoggerFactory.getLogger.withMarker(bobj("program" -> "AAAParser"))
  override def main: Opts[IO[ExitCode]] =
    uberApp.map {
      case cmd: CleanCmd =>
        logger.info("Args $cmd")
        CleanMain.process(cmd) *> IO.pure(ExitCode.Success)

      case cmd: StatsCmd =>
        logger.info("Args $cmd")
        StatsMain.process(cmd) *> IO.pure(ExitCode.Success)

    }
}
