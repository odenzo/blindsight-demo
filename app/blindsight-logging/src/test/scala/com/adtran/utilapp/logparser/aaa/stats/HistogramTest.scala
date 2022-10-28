package com.adtran.utilapp.logparser.aaa.stats

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

class HistogramTest extends munit.CatsEffectSuite {
  private val logger = LoggerFactory.getLogger

  test("Regex Single") {
    val path        = "blahblah/foo=bar"
    val histo       = new Histogram()
    val res: String = histo.sanitizePathParams(path)
    assertEquals(res, "blahblah/foo=XXX")
  }

  test("Regex Double") {
    val path        = "blahblah/foo=bar/goo=foobar"
    val histo       = new Histogram()
    val res: String = histo.sanitizePathParams(path)
    println(res)
    assertEquals(res, "blahblah/foo=XXX/goo=XXX")
  }
}
