package com.adtran.utilapp.logparser.aaa.stats

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.IO
import com.adtran.utilapp.logparser.aaa.models.AAALogLine
import fs2.Stream
import fs2.io.file.Path
import org.http4s.Uri

import java.time.Instant
import scala.collection.concurrent.TrieMap
import scala.util.matching.Regex

/** Class based state instead of stream based to count when each path is called, retaining the timestamp called */
class Histogram() {
  private val histoMap: TrieMap[String, List[Instant]] = scala.collection.concurrent.TrieMap.empty[String, List[Instant]]

  def updateStoredValue(ts: Instant)(entry: Option[List[Instant]]): Option[List[Instant]] = entry match {
    case Some(existing: List[Instant]) => Some(ts :: existing)
    case None                          => List(ts).some
  }

  /** We want to ignore any parameters and any foo/bar/item=someting, turning something to ZZZ always. */
  def noteEvent(ev: AAALogLine): Option[List[Instant]] = {
    val keyRaw: String = ev.path.toString
    val regex          = "(.+?)[/$]".r
    val cleanPath      = sanitizePathParams(ev.path.toString)
    val key            = s"${cleanPath}_${ev.method.name}"
    histoMap.updateWith(key)(updateStoredValue(ev.ts))

  }

  def calculate(src: Path): IO[(MinMax, TrieMap[String, List[Instant]])] = {
    val in: Stream[IO, AAALogLine] = AAACallAnalysis.sourceStream[AAALogLine](src)
    val total: Stream[IO, MinMax]  = in
      .evalTap(ev => IO(noteEvent(ev)))
      .map(v => v.ts)
      .through(AAACallAnalysis.keepMinMax)

    total.compile.lastOrError.tupleRight(histoMap)

  }

  def sanitizePathParams(path: String): String = {
    val regex                           = "=(.+?)(/|$)".r.unanchored
    val replacer: Regex.Match => String = (m: Regex.Match) => {
      val delim = m.group(2)
      s"=XXX$delim"
    }
    regex.replaceAllIn(path, replacer)
  }
}
