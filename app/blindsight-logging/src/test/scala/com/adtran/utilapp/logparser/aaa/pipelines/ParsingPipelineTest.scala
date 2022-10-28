package com.adtran.utilapp.logparser.aaa.pipelines

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.parse.Parser
import cats.syntax.all.*
import com.adtran.utilapp.logparser.aaa.TestHelpers
import com.adtran.utilapp.logparser.aaa.models.StreamContext
import com.adtran.utilapp.logparser.aaa.parsers.AAALineParsers
import com.adtran.utilapp.logparser.aaa.pipelines.ParseIntoRowsPipeline
import com.comcast.ip4s.Ipv4Address
import com.tersesystems.blindsight.DSL.{*, given}
import com.tersesystems.blindsight.{*, given}
import fs2.*
import fs2.io.file.{Files, Path}
import munit.Assertions

import java.io.InputStream
class ParsingPipelineTest extends munit.CatsEffectSuite {
  private val logger = LoggerFactory.getLogger

  val noObject: String =
    """2022-09-28T14:20:57.236+0000,reillyj3_adt,192.168.1.250,GET,/api/restconf/data/adtran-cloud-platform-uiworkflow:transitions/transition=da5cc28a-ea49-4c8c-8481-540cfcda9431,,200 OK
      |""".stripMargin

  val withObject: String =
    """2022-09-28T14:20:56.930+0000,reillyj3_adt,192.168.1.250,POST,/api/restconf/operations/adtran-cloud-platform-uiworkflow:create,{"input": {"interface-context": {"interface-name": "BAAMZT-OLT4-48", "interface-type": "gpon", "number-of-lower-layer-interfaces": 0, "profile-vector-name": "OLT_Leaf_GPON_Interface_Vector"}}},200 OK
      |""".stripMargin



  test("Re-Lining") {
    TestHelpers
      .loadResourceAsLines("/TestLogs.log")
      .through(ParseIntoRowsPipeline.pipeline)
      .debug(v => v.payload, s => logger.info(s))
      .compile
      .drain
  }
}
