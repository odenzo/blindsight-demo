package com.adtran.utilapp.logparser.aaa.errors

import cats.parse.Parser

import scala.util.Try

case class ParsingException(txt: String, error: Parser.Error) extends Throwable {
  override def getMessage: String = s"Parsing Source:[$txt]\n Got Error:\n${pprint.apply(error)} Input: ${pprint.apply(error.input)}"
}

object ParsingException:
  /** Checks parsing result and throws ParsingException or returns success value. */
  def createAndThrow(src: String, err: Parser.Error) = throw ParsingException(src, err)
