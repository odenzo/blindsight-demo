package com.adtran.utilapp.logparser.aaa

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.IO
import com.adtran.utilapp.logparser.aaa.models.*
import com.adtran.utilapp.logparser.aaa.pipelines.ParsingPipeline
import com.tersesystems.blindsight.{LoggerFactory, bobj}
import fs2.io.file.*
import com.tersesystems.blindsight.DSL.{*, given}
import com.adtran.utilapp.logparser.aaa.parsers.AAALineParsers.given

class LogsTransformer(srcDir: Path, destDir: Path, filterFn: Path => Boolean) {
  private val logger          = org.log4s.getLogger
  logger.info("Constructing LogsTransformer")
  private lazy val resultFile = destDir.resolveSibling("results.json")
  private lazy val errorFile  = destDir.resolveSibling("errors.json")

  def deleteFile(f: Path): IO[Boolean] = Files[IO].deleteIfExists(f)

  def process(): IO[Unit] = {
    val pipelinesResults = for {
      _     <- deleteFile(resultFile)
      _     <- deleteFile(errorFile)
      files <- selectFiles(srcDir)
      res   <- files.traverse(f => parseFile(f))
    } yield res

    pipelinesResults.void
  }

  private def parseFile(f: Path): IO[Vector[Either[StreamContext[ParsingError], StreamContext[AAALogLine]]]] = {
    ParsingPipeline
      .parseFile[AAALogLine](f, errorFile, resultFile)
      .compile
      .toVector
      .flatTap { v =>
        val (errors, correct) = v.partition(_.isLeft)
        IO.blocking(logger.info(s"$f Errors: ${errors.size}   Correct: ${correct.size}"))
      }
  }

  /** Determines which unpacked log files to process in (log) time based order. */
  def selectFiles(srcDir: Path): IO[Seq[Path]] = {
    import scala.jdk.CollectionConverters.given
    logger.info(s"Selecting Files in $srcDir")
    val filesFiltered = IO {
      Files[IO]
        .list(srcDir)
        .filter(filterFn) // Filters just the name part
        .debug(v => s"Rollover Entry: ${v.extName}", s => logger.warn(s))
        .filter(f => f.extName == ".log" || f.extName.drop(1).toIntOption.isDefined)
    }

    filesFiltered
      .flatMap { (str: fs2.Stream[IO, Path]) => str.compile.toVector }
      .map((files: Seq[Path]) => files.sortBy(_.extName.drop(1).toIntOption.getOrElse(0)))
      .flatTap(fl => IO(logger.debug(s"Sorted Files: ${pprint.apply(fl)}")))

  }

}
