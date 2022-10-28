package com.adtran.utilapp.logparser.aaa.parsers

import cats.parse.*
import cats.effect.*
import cats.effect.syntax.all.*

import cats.*
import cats.data.*
import cats.syntax.all.*

import java.time.Instant
import scala.deriving.Mirror
import scala.util.chaining.*
import com.adtran.utilapp.logparser.aaa.models.*
import com.comcast.ip4s.Ipv4Address
import io.circe.Json
import org.http4s.Method
import com.adtran.utilapp.logparser.aaa.parsers.ParserAtoms.*

import org.http4s.Uri.Path

object AAALineParsers {

  /** Path has kComman so take until JsonObject or empty, which gets encoded of kCommaKcomma or kComma kOpenBracket which is very specific
    * to the AAA log file format.
    */
 given lineParser: Parser[AAALogLine] = {
    val lp: Parser[(Instant, String, Ipv4Address, Method, Path, Option[Json], String)] =
      (
        timestamp <* kComma,
        name <* kComma,
        ipV4 <* kComma,
        httpMethod <* kComma,
        httpPath <* kComma,
        optJsonObject <* kComma,
        httpStatus <* Parser.end
      ).tupled

    lp.map { tup => summon[Mirror.Of[AAALogLine]].fromProduct(tup) }
      .withContext("AAALogLine")
  }


}
