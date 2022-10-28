package com.adtran.utilapp.logparser.aaa

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.adtran.utilapp.logparser.aaa.codecs.BlindsightCodecs.given
import com.adtran.utilapp.logparser.aaa.codecs.ExternalCirceCodecs.given
import com.adtran.utilapp.logparser.aaa.models.{AAALogLine, ParsingError, StreamContext}
import com.adtran.utilapp.logparser.aaa.parsers.AAALineParsers.given
import com.adtran.utilapp.logparser.aaa.pipelines.ParsingPipeline
import com.adtran.utilapp.logparser.aaa.stats.{Histogram, MinMax}
import com.tersesystems.blindsight.DSL.{*, given}
import com.tersesystems.blindsight.{*, given}
import fs2.io.file
import os.CommandResult

import java.nio.file.Path
import java.time.Instant
import scala.collection.concurrent.TrieMap
import scala.io.Source
import scala.util.chaining.{*, given}

object StatsMain {
  private val logger = org.log4s.getLogger
  // private val cLogger = LoggerFactory.getLogger

  def process(cmd: StatsCmd): IO[Unit] =

    logger.warn(s"Running Main:process() $cmd")

    val stats = for {
      resultsFile <- IO(cmd.srcDir.resolve("results.json"))
      stats       <- doStatistics(resultsFile)
      _            = logger.info("Processing Complete")
    } yield stats

    stats.map { case (minmax: MinMax, histo: TrieMap[String, List[Instant]]) =>
      val paths = histo.keySet
      logger.info(s"Total Number of Unique Paths: ${paths.size}")
      histo.toVector
        .filter(_._1.contains("uiworkflow"))
        .sortBy(_._2.length) // Lowest to hights
        .map { case (path, calls) =>
          s""" ${calls.length}\t: $path  ==>  ${calls.min} <-> ${calls.max} """.stripMargin
        }
        .pipe(vd => logger.info(s"""Basic Stats::\n${vd.mkString("\n")}"""))
    }

  def doStatistics(f: fs2.io.file.Path): IO[(MinMax, TrieMap[String, List[Instant]])] = {
    val worker                                                = new Histogram()
    val results: IO[(MinMax, TrieMap[String, List[Instant]])] = worker.calculate(f)
    results.flatTap { case (minmax, histomap) =>
      IO(logger.info(s"Min-Max ${minmax.min.toString} -> ${minmax.max.toString} "))
    }
  }

}
