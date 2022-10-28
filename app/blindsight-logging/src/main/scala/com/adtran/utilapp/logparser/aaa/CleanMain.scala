package com.adtran.utilapp.logparser.aaa

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import os.CommandResult

import java.nio.file.Path
import scala.util.chaining.*
import codecs.BlindsightCodecs.given
import com.tersesystems.blindsight.{*, given}
import com.tersesystems.blindsight.DSL.{*, given}
import com.adtran.utilapp.logparser.aaa.codecs.ExternalCirceCodecs.given
import com.adtran.utilapp.logparser.aaa.models.{AAALogLine, ParsingError, StreamContext}
import com.adtran.utilapp.logparser.aaa.parsers.AAALineParsers.given
import com.adtran.utilapp.logparser.aaa.pipelines.ParsingPipeline
import com.adtran.utilapp.logparser.aaa.stats.{Histogram, MinMax}
import fs2.io.file

import java.time.Instant
import scala.collection.concurrent.TrieMap
import scala.util.chaining.given
import scala.io.Source

object CleanMain {
  private val logger = org.log4s.getLogger
  // private val cLogger = LoggerFactory.getLogger

  /** Files of interest are this with possibly an appended ".NN" which is non-zero padded integer */
  private val filePrefix = "firefly-aaa-accounting.log"

  /** Filter function for log files. Assyme all follow the patter of <LogName>.log.[Int] with gz appended for compressed */
  private val filterFn = (f: fs2.io.file.Path) => {
    f.fileName.toString.startsWith(filePrefix)
  }

  def process(cmd: CleanCmd): IO[Unit] =

    logger.warn(s"Running Main:process() $cmd")

    for {
      _           <- GunzipFiles.unCompressFilesIn(cmd.srcDir, filePrefix)
      _           <- new LogsTransformer(cmd.srcDir, cmd.destDir, filterFn).process()
      resultsFile <- IO(cmd.destDir.resolve("results.json"))
      _            = logger.info("Processing Complete")
    } yield ()

}
