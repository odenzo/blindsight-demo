package com.adtran.utilapp.logparser.aaa.parsers

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.parse.Parser
import cats.syntax.all.*
import com.adtran.utilapp.logparser.aaa.parsers.AAALineParsers
import com.comcast.ip4s.Ipv4Address
import com.tersesystems.blindsight.DSL.{*, given}
import com.tersesystems.blindsight.{*, given}
import com.tersesystems.blindsight.ArgumentEnrichment.RichToArgument
import munit.Assertions
import com.adtran.utilapp.logparser.aaa.codecs.BlindsightCodecs.{given, *}

class AAALineParsersTest extends munit.FunSuite {
  private val logger   = LoggerFactory.getLogger
  logger.warn("Constructed")
  val noObject: String =
    """2022-09-28T14:20:57.236+0000,reillyj3_adt,192.168.1.250,GET,/api/restconf/data/adtran-cloud-platform-uiworkflow:transitions/transition=da5cc28a-ea49-4c8c-8481-540cfcda9431,,200 OK
      |""".stripMargin

  val withObject: String =
    """2022-09-28T14:20:56.930+0000,reillyj3_adt,192.168.1.250,POST,/api/restconf/operations/adtran-cloud-platform-uiworkflow:create,{"input": {"interface-context": {"interface-name": "BAAMZT-OLT4-48", "interface-type": "gpon", "number-of-lower-layer-interfaces": 0, "profile-vector-name": "OLT_Leaf_GPON_Interface_Vector"}}},200 OK
      |""".stripMargin

  test("Plain Line") {
    AAALineParsers.lineParser.parse(noObject) match {
      case Left(err: Parser.Error) =>
        logger.error("Failed {}", err.asArgument)
        fail(s"Parsing: -> ${pprint(err)} ${err._2}")
      case Right(res)              =>
        logger.info(s"Results: ==> ${pprint.apply(res)}")
        Assertions.assert(res._1.isEmpty)
    }

  }




}
