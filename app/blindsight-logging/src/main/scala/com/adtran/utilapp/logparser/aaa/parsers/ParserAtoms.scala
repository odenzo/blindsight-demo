package com.adtran.utilapp.logparser.aaa.parsers

import cats.parse.Parser.With1
import cats.parse.{Numbers, Parser, Parser0, Rfc5234}
import com.comcast.ip4s.Ipv4Address
import com.tersesystems.blindsight.*
import com.tersesystems.blindsight.DSL.*

import io.circe.Json
import org.http4s.{Method, Uri}

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import scala.util.{Failure, Success, Try}

object ParserAtoms {
  private val cLogger = LoggerFactory.getLogger
  // Validators

  /** v is an Int stricly within the given range */
  def intInside(a: Int, b: Int)(v: String): Boolean =
    val i = v.toInt
    i > a && i < b

  /** v is an Int within the given range, including the endpoint values */
  def intWithin(a: Int, b: Int)(v: String): Boolean =
    val i = v.toInt
    i >= a && i <= b
  def exactlyDigits(count: Int): Parser[String]     = Numbers.digit.repExactlyAs(count)

  /** EOL character LF/CRLF/CR tried in order */
  val kEOL: Parser[Unit] = (Rfc5234.lf | Rfc5234.crlf | Rfc5234.cr).void.withContext("EOL")

  val kComma: Parser[Unit]               = Parser.char(',').void.withContext("Comma")
  val kEmptyNextField: Parser[Unit]      = kComma.rep(2, 2).void.withContext("Empty CSV Next")
  val kJsonObjectNextField: Parser[Unit] = Rfc5234.wsp.rep0.with1 *> Parser.char('{').withContext("Delimeted by JSONObject")
  val kDash: Parser[Unit]                = Parser.char('-').withContext("kDash")
  val kDot: Parser[Unit]                 = Parser.char('.').withContext("kDot")
  val listSep: Parser[Unit]              = kComma // .surroundedBy(Rfc5234.wsp.soft).void
  val httpMethod: Parser[Method]         = Parser.until(kComma).mapFilter(s => Method.fromString(s).toOption)
  val kUntilEnd: Parser0[String]         = Parser.until0(Parser.end)
  val endOfHttpPathField: Parser[Unit]   = kComma *> kComma.orElse(kJsonObjectNextField)

  /** Sigh, the URI can have a comma in it, specifically the GET parameter list. */
  val httpPath: Parser[Uri.Path] = Parser.until(endOfHttpPathField).mapFilter { s =>
    val opt = Try(Uri.Path.unsafeFromString(s)).toOption
    // logger.info(s"HTTP Path Opt: $opt")
    opt
  }

  val ipV4: Parser[Ipv4Address] = Parser
    .until(kComma)
    .mapFilter(v => com.comcast.ip4s.Ipv4Address.fromString(v))
    .withContext("IPV4")
  // Meh, non-standard timestamps not as Instant format w/ Z for UTC.

  /** Timestamp/aka ISO_INSTANT please! 2022-09-28T14:20:57.236+0000.
    */
  val adtranDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  def parseBadLogTimestamp(ts: String): Option[Instant] = Try {
    // logger.info(s"Decoding TStamp $ts")
    // DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(ts, Instant.from)
    val data          = adtranDateTimeFormatter.parse(ts)
    val epochSeconds  = data.getLong(ChronoField.INSTANT_SECONDS)
    val subsecondNano = data.getLong(ChronoField.NANO_OF_SECOND)
    Instant.ofEpochSecond(epochSeconds, subsecondNano)
  } match
    case Success(value)     => Some(value)
    case Failure(exception) =>
      // logger.error(s"Failure Parsing TStamp $ts", exception)
      None

  val optJsonObject: With1[Option[Json]] = JsonParser.jsonParser
    .orElse(Parser.unit)
    .map {
      case j: Json => Some(j)
      case _       => None
    }
    .withContext("JsonObject")
    .with1

  val timestamp: Parser[Instant] = Parser
    .until(kComma)
    .mapFilter(ts => parseBadLogTimestamp(ts))
    .withContext("timestamp")

  val name: Parser[String] = Parser.until(kComma).withContext("Name")

  val httpStatus: Parser[String] = Parser.anyChar.repUntil(kEOL).string // Hack

  val kEmptyLine: Parser0[Unit] = Rfc5234.wsp.repUntil0(Parser.end).void

  val startWithTimestamp: Parser0[Instant] = Parser.start *> Rfc5234.wsp.rep0 *> timestamp

  /*
cala> type Flat[T <: Tuple] <: Tuple = T match
     |   case EmptyTuple => EmptyTuple
     |   case h *: t => h match
     |     case Tuple => Tuple.Concat[Flat[h], Flat[t]]
     |     case _     => h *: Flat[t]
     |

scala> def flat[T <: Tuple](v: T): Flat[T] = (v match
     |   case e: EmptyTuple => e
     |   case h *: ts => h match
     |     case t: Tuple => flat(t) ++ flat(ts)
     |     case _        => h *: flat(ts)).asInstanceOf[Flat[T]]
def flat[T <: Tuple](v: T): Flat[T]

scala> def ft[A <: Tuple, C](f: Flat[A] => C): A => C = a => f(flat(a))
def ft[A <: Tuple, C](f: Flat[A] => C): A => C

scala> val f0: ((Int, Int, Int)) => Int = x => x._1 + x._2 + x._3


scala> def g0(f: ((Int, (Int, Int))) => Int): Int = f(1,(2,3))

scala> g0(ft(f0))
val res0: Int = 6
   */
}
