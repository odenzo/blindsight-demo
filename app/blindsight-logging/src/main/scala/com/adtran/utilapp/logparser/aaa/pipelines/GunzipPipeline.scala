package com.adtran.utilapp.logparser.aaa.pipelines

import cats.Functor
import cats.effect.IO
import cats.parse.Parser
import cats.parse.Parser.Error
import com.adtran.utilapp.logparser.aaa.models.{ParsingError, StreamContext}
import com.tersesystems.blindsight.LoggerFactory
import fs2.io.file.{Files, Flags, Path}
import fs2.text
import fs2.compression.{*, given}
import fs2.compression.Compression.{*, given}
import io.circe.Encoder

import java.nio.file.OpenOption

object GunzipPipeline {
  type MyPipe[F[_], -I, +O] = fs2.Stream[F, I] => fs2.Stream[F, O]

  private val logger = LoggerFactory.getLogger

  /** Gunzip Streaming Uncompressor that reads as UTF-8 charaters broken in lines into my StreamContext */
  def gunzipUTF8File(theFile: Path): fs2.Stream[IO, StreamContext[String]] = {
    import fs2.compression.{given, *}
    Files[IO]
      .readAll(theFile)
      .through(Compression[IO].gunzip())
      .flatMap(_.content)
      .through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .zipWithIndex
      .map { case (s: String, indx) => StreamContext(s, indx, theFile) }
      .debug(ctx => s"MainStreamLine: ${ctx.file}:${ctx.line}\n${ctx.payload}", str => logger.warn(str))
  }

  /** Reads, Gunzip and pipes to binary file output (overwriting existing) */
  def gunzipBinaryFile(theFile: Path, output: Path): fs2.Stream[IO, Nothing] = {
    Files[IO]
      .readAll(theFile)
      .through(Compression[IO].gunzip())
      .flatMap(_.content)
      .through(Files[IO].writeAll(output, Flags.Write))
      .debug(x => s"Done with $theFile to $output dir", s=> logger.warn(s))
  }

  val  gunzipBinPipe :fs2.Stream[IO,(Path,Path)] => fs2.Stream[IO,Path]    =
    (x: fs2.Stream[IO, (Path,Path)]) => {
      x.flatMap {  (in: Path, out:Path)
 =>
        Files[IO]
          .readAll(in)
          .through(Compression[IO].gunzip())
          .flatMap(_.content)
          .through(Files[IO].writeAll(out, Flags.Write))
          .debug(x => s"Done with $in to $out", s => logger.warn(s))
          .as(out)
      }
    }

}
