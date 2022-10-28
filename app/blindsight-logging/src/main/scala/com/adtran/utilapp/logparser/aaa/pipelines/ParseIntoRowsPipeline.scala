package com.adtran.utilapp.logparser.aaa.pipelines

import cats.effect.*
import cats.effect.syntax.all.*

import cats.*
import cats.data.*
import cats.syntax.all.*

import cats.effect.IO
import com.adtran.utilapp.logparser.aaa.models.StreamContext
import com.adtran.utilapp.logparser.aaa.parsers.ParserAtoms
import com.tersesystems.blindsight.LoggerFactory
import fs2.{Chunk, Pipe, Pull, Stream}
import cats.syntax.all.given

import scala.annotation.tailrec

/** Parsing for AAA Accounting Logs (as od Sep 2022) this tries to pass the full log message. This is steaming... */
object ParseIntoRowsPipeline {
  private val cLogger = LoggerFactory.getLogger

  val pipeline: Pipe[IO, StreamContext[String], StreamContext[String]] = (in: Stream[IO, StreamContext[String]]) => {
    cLogger.debug("Starting to Setup Re-Aggregate/Combine Lines")
    in
      .debug(s => s"Raw Line: $s", fmt => cLogger.debug(fmt))
      .map(identity)
      .through(aggregateFullLogLine)
      .debug(s => s"Raw Line: $s", fmt => cLogger.debug(fmt))

  }

  private def startsWithTimestamp(s: String): Boolean = ParserAtoms.startWithTimestamp.parse(s).isRight

  private[pipelines] val aggregateFullLogLine: Stream[IO, StreamContext[String]] => Stream[IO, StreamContext[String]] =
    (inStream: Stream[IO, StreamContext[String]]) => {

      def go(stream: Stream[IO, StreamContext[String]], state: Option[StreamContext[String]]): Pull[IO, StreamContext[String], Unit] = {
        stream.pull.uncons1.flatMap { // Get the next element in stream
          case None => // End of Stream, emit pending state (logline) if stored
            state.fold(Pull.done)(v => Pull.output1(v) >> Pull.done)

          case Some((element: StreamContext[String], remStream)) if startsWithTimestamp(element.payload) => // Previous record closed
            if state.isDefined then
              val thePull: Pull[Nothing, StreamContext[String], Unit] = Pull.output1(state.get) // No Chunks
              thePull >> go(remStream, element.some)
            else go(remStream, element.some)

          case Some((element, remStream)) =>
            // We got a new element, but it is a run-on line. We *must* have a state so combine the lines (with no line delimeter)
            val updatedState: Option[StreamContext[String]] =
              state.map(ctx => ctx.transform(payload => payload + element.payload))
            go(remStream, updatedState)
        }
      }

      go(inStream, Option.empty).stream

    }

}
