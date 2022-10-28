package com.adtran.utilapp.logparser.aaa.pipelines

import cats.Functor
import cats.effect.IO
import cats.parse.Parser
import cats.parse.Parser.Error
import cats.syntax.all.given
import com.adtran.utilapp.logparser.aaa.models.{ParsingError, StreamContext}
import com.tersesystems.blindsight.LoggerFactory
import fs2.io.file.{Files, Flags, Path}
import fs2.{Chunk, text}
import io.circe.{Decoder, Encoder, JsonObject}
import io.circe.syntax.{*, given}

import java.util.zip.DeflaterOutputStream
import com.adtran.utilapp.logparser.aaa.parsers.AAALineParsers.given

/** Parsing for AAA Accounting Logs (as od Sep 2022) this tries to pass the full log message */
object ParsingPipeline {
  type MyPipe[F[_], -I, +O] = fs2.Stream[F, I] => fs2.Stream[F, O]

  private val logger = org.log4s.getLogger

  /** Parses files writing to "results.json" and "errors.json" in same directory. ASSUMPTION that f is in destination directory since its
    * gunziped
    */
  def parseFile[T: Parser: Encoder: Decoder](
      f: fs2.io.file.Path,
      errors: Path,
      success: Path
  ): fs2.Stream[IO, Either[StreamContext[ParsingError], StreamContext[T]]] = {
    import io.circe.Encoder.AsArray.importedAsArrayEncoder
    import io.circe.Encoder.AsObject.importedAsObjectEncoder
    import io.circe.Encoder.AsRoot.importedAsRootEncoder
    given e: Encoder[Either[ParsingError, T]]   = Encoder.encodeEither[ParsingError, T]("error", "result")
    given d: Decoder[Either[ParsingError, T]]   = Decoder.decodeEither[ParsingError, T]("error", "result")
    given ctxEncoder: Encoder[StreamContext[T]] = summon[Encoder[StreamContext[T]]]

    val lineParser = summon[Parser[T]]
    val theFile    = f

    import StreamContext.given
    val errorStream: fs2.Stream[IO, StreamContext[ParsingError]] => fs2.Stream[IO, Nothing] =
      (in: fs2.Stream[IO, StreamContext[ParsingError]]) => {
        in.map { (ctx: StreamContext[ParsingError]) =>
          JsonObject("error" -> ctx.payload.asJson, "file" -> ctx.file.toString.asJson, "line" -> ctx.line.asJson).asJson.noSpaces + "\n"
        }.through(text.utf8.encode)
          .through(Files[IO].writeAll(errors, Flags.Append))
      }

    def successStream: MyPipe[IO, StreamContext[T], Nothing] =
      in => {
        in.map { (ctx: StreamContext[T]) => ctx.payload.asJson.noSpaces + "\n" }
          .through((v: fs2.Stream[IO, String]) => fs2.text.utf8.encode(v))
          .through(Files[IO].writeAll(success, Flags.Append))
      }

    Files[IO]
      .readUtf8Lines(theFile)
      .debug(s => s"RawLine:(${s.length}) [$s]", str => logger.debug(str))
      .zipWithIndex
      .filter((v: (String, Long)) => !v._1.isBlank)
      .map { case (s, indx) => StreamContext(s, indx, theFile) }
      .through(ParseIntoRowsPipeline.pipeline)
      .map { (ctx: StreamContext[String]) =>
        val parseResult: Either[ParsingError, T] = lineParser.parse(ctx.payload) match {
          case Left(err: Error)      => Left(ParsingError.from(err))
          case Right(v: (String, T)) => Right(v._2)
        }

        ctx.transform(_ => parseResult)
      }
      .debug((v: StreamContext[Either[ParsingError, T]]) => v.toString, s => logger.debug(s))
      .map { v =>
        v.payload match {
          case Left(err: ParsingError) => v.copy(payload = err).asLeft
          case Right(value: T)         => v.copy(payload = value).asRight
        }
      }
      .observeEither[StreamContext[ParsingError], StreamContext[T]](errorStream, successStream)
  }

}
