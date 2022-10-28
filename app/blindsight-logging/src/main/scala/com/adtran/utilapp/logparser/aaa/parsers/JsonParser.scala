package com.adtran.utilapp.logparser.aaa.parsers

import cats.parse.{Parser as P}
import cats.parse.{Numbers, Parser0, Rfc5234}
import io.circe.{Json, JsonNumber, JsonObject}
import io.circe.syntax.given

object JsonParser {
  private[this] val whitespace: P[Unit]         = P.charIn(" \t\r\n").void
  private[this] val whitespaces0: Parser0[Unit] = whitespace.rep0.void

  /** Double quotes String contents, \" internal not handled. */
  val validStrChar: P.With1[String] = P.anyChar.repUntil0(Rfc5234.dquote).map(v => if v.isEmpty then "" else v.mkString).with1

  val jsonString: P[String] = validStrChar.surroundedBy(Rfc5234.dquote).surroundedBy(whitespaces0)

  val parser: P[Json] = P.recursive[Json] { recurse =>
    val pnull: P[Json] = P.string("null").as(Json.Null)
    val bool: P[Json]  = P.string("true").as(Json.True).orElse(P.string("false").as(Json.False))
    val str: P[Json]   = jsonString.map(Json.fromString)
    val num: P[Json]   = Numbers.jsonNumber.map(v => JsonNumber.fromString(v).fold(throw Throwable("Impossible Number"))(v => v.asJson))

    val listSep: P[Unit] =
      P.char(',').soft.surroundedBy(whitespaces0).void

    def rep[A](pa: P[A]): Parser0[List[A]] =
      pa.repSep0(listSep).surroundedBy(whitespaces0)

    val list: P[Json] = rep(recurse).with1
      .between(P.char('['), P.char(']'))
      .map { (vs: List[Json]) => vs.asJson }

    val kv: P[(String, Json)] =
      jsonString ~ (P.char(':').surroundedBy(whitespaces0) *> recurse)

    val obj = rep(kv).with1
      .between(P.char('{'), P.char('}'))
      .map { vs => JsonObject.fromIterable(vs).asJson }

    P.oneOf(str :: num :: list :: obj :: bool :: pnull :: Nil)
  }

  // any whitespace followed by json followed by whitespace followed by end
  val jsonParser: P[Json] = whitespaces0.with1 *> parser <* (whitespaces0) // Not, not seeking to end of "file" and embedded in C

}
