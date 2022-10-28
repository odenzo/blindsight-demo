package com.adtran.utilapp.logparser.aaa.stats

import fs2.*
import fs2.io.file.*
import _root_.io.circe.*
import _root_.io.circe.fs2.*
import _root_.io.circe.syntax.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.adtran.utilapp.logparser.aaa.models.AAALogLine
import org.log4s.Logger

import java.time.Instant
import scala.::
import scala.collection.concurrent.TrieMap

/** What do we want: Bucket counts of alls toe UIWorkflow (method, url). Timestpan (minlogtime, maxlogtime) */
object AAACallAnalysis {
  val logger: Logger = org.log4s.getLogger

  /** Source stream hooked up via histogram calculate */
  def sourceStream[T: Decoder](srcFile: Path): Stream[IO, T] =
    Files[IO]
      .readUtf8Lines(srcFile)
      .through(stringStreamParser)
      .through(decoder[IO, T])

  val keepMinMax: Pipe[IO, Instant, MinMax] = (in: Stream[IO, Instant]) => {
    in.scan(MinMax.openRange)(MinMax.update)
  }

}
