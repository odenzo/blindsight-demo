package com.adtran.utilapp.logparser.aaa.models

import cats.effect.*
import cats.effect.syntax.all.*

import cats.*
import cats.data.*
import cats.syntax.all.*

import cats.parse.Parser
import fs2.io.file.Path as FS2Path
import io.circe.generic.semiauto.{given, *}
import io.circe.generic.auto.{given, *}
import io.circe.Derivation.{given, *}
import io.circe.{given, *}
import com.adtran.utilapp.logparser.aaa.codecs.ExternalCirceCodecs.given
import scala.util.Try
import io.circe.syntax.*
/** Used instead of logging context basically, for FS2 Streams which still don't have context or mutable context Dig out the old typeclass
  * for MutableContext
  */
final case class StreamContext[T: Encoder:Decoder](payload: T, line: Long, file: FS2Path) {
  def transform[B:Encoder:Decoder](fn:T=>B): StreamContext[B] = this.copy(payload = fn(payload))
}

object StreamContext {
  //  given streamContextCodec: Codec[StreamContext[String]]      = Codec.from(deriveDecoder[StreamContext], deriveEncoder[StreamContext])
  //  given streamContextPathCodec: Codec[StreamContext[FS2Path]] = Codec.from(deriveDecoder[StreamContext], deriveEncoder[StreamContext])

  private def decoder[T:Encoder:Decoder]: Decoder[StreamContext[T]] = new Decoder[StreamContext[T]] {
    final def apply(c: HCursor): Decoder.Result[StreamContext[T]] =
      for {
        line <- c.downField("line").as[Long]
        file <- c.downField("file").as[FS2Path]
        payload <- c.downField("payload").as[T]
      } yield {
        StreamContext(payload, line, file)
      }
  }

  private def encoder[T:Encoder:Decoder]: Encoder[StreamContext[T]] = Encoder.AsObject.instance { (v: StreamContext[T]) =>
      JsonObject("payload" -> v.payload.asJson, "file" -> v.file.asJson, "line" -> v.line.asJson)
  }

  def codec[T:Codec]: Codec[StreamContext[T]] = Codec.from(this.decoder[T],this.encoder[T])

}
