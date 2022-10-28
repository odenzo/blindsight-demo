package com.adtran.utilapp.logparser.aaa

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.adtran.utilapp.logparser.aaa.models.StreamContext
import com.tersesystems.blindsight.LoggerFactory
import fs2.*
import fs2.io.file.Path

import java.io.InputStream

object TestHelpers {

  private val logger                                                       = LoggerFactory.getLogger
// Stream.bracket(acquire)(release).flatMap(conn => savedJlActors)
  def loadResourceAsLines(name: String): Stream[IO, StreamContext[String]] = {
    val is: IO[InputStream] = IO.blocking(getClass.getResourceAsStream(name))

    fs2.io
      .readInputStream(is, 1024, true)
      .through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .debug(s => s"RawLine:(${s.length}) [$s]", str => logger.warn(str))
      .zipWithIndex
      .filter((v: (String, Long)) => !v._1.isBlank)
      .map { case (s, indx) => StreamContext(s, indx, Path(name)) }
  }

}
