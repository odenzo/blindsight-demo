package com.adtran.utilapp.logparser.aaa.codecs

import com.comcast.ip4s.Ipv4Address
import io.circe.{Codec, Decoder, Encoder, JsonObject}
import io.circe.syntax.given
import org.http4s.{Method, ParseFailure, Uri}

import scala.util.Try
import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.parse.Parser
import cats.syntax.all.*
import fs2.io.file.{Files, Flags, Path as FS2Path}
import java.nio.file.Path
import java.net.URI

/** Some things, like IPV4Address are defined by http4s and comcast and need Circe CODECs */
object ExternalCirceCodecs {

  given comcastUPv4Codec: Codec[Ipv4Address] = Codec.from(
    Decoder.decodeString.emap(s =>
      Ipv4Address.fromString(s) match
        case Some(value) => Right(value)
        case None        => Left("Invalid IPv4")
    ),
    Encoder.encodeString.contramap(addr => addr.toString)
  )

  given httpMethodCodec: Codec[Method] = Codec.from(
    Decoder.decodeString.emap(s => Method.fromString(s).leftMap((err: ParseFailure) => err.message)),
    Encoder.encodeString.contramap(method => method.name)
  )

  given uriPath: Codec[org.http4s.Uri.Path] = Codec.from(
    Decoder.decodeString.emapTry(s => Try(Uri.Path.unsafeFromString(s))),
    Encoder.encodeString.contramap(path => path.renderString)
  )

  given nioPath: Codec[java.nio.file.Path] =
    Codec.from(Decoder.decodeURI.emapTry[Path]((uri: URI) => Try { Path.of(uri) }), Encoder.encodeURI.contramap[Path](p => p.toUri))

  given fs2PathCodec: Codec[fs2.io.file.Path] =
    Codec.from(
      Decoder.decodeString.emapTry(n => Try { FS2Path(n) }),
      Encoder.encodeString.contramap((v: fs2.io.file.Path) => v.absolute.toString)
    )

  given parserErrorEncoder: Encoder[cats.parse.Parser.Error] = Encoder.instance { err =>
    JsonObject("line" -> err._1.asJson, "input" -> err.input.asJson, "expected" -> err.expected.toString.asJson).asJson
  }
}
