package com.adtran.utilapp.logparser.aaa.models

import cats.effect.*
import cats.effect.syntax.all.*

import cats.*
import cats.data.*
import cats.syntax.all.*

import io.circe.Derivation.{given, *}
import io.circe.{given, *}

/** Internal representation of cats.parse.Parsing.Error with Codec */
case class ParsingError(input: Option[String], line: Int, details: NonEmptyList[String]) derives Codec.AsObject

object ParsingError:
  def from(e: cats.parse.Parser.Error): ParsingError = ParsingError(e.input, e._1, e.expected.map(exp => exp.show))
