package com.adtran.utilapp.logparser.aaa.models

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.adtran.utilapp.logparser.aaa.codecs.ExternalCirceCodecs.given
import com.adtran.utilapp.logparser.aaa.parsers.ParserAtoms.*
import com.comcast.ip4s.Ipv4Address
import io.circe.*
import io.circe.generic.semiauto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*
import org.http4s.*
import org.http4s.Uri.Path

import java.time.Instant

/** Represents a Types and Parsed AAA Log Line execept the ones with multilines JSON serialized into them :-( */
case class AAALogLine(ts: Instant, user: String, ip: Ipv4Address, method: Method, path: Path, body: Option[Json], httpStatus: String)
    derives Codec.AsObject
