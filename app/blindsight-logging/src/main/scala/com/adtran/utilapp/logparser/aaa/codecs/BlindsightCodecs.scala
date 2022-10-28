package com.adtran.utilapp.logparser.aaa.codecs

import cats.parse.Parser
import com.adtran.utilapp.logparser.aaa.CleanCmd
import com.tersesystems.blindsight.{*, given}
import com.tersesystems.blindsight.DSL.{*, given}
import io.circe.JsonObject
import org.slf4j.{Marker, MarkerFactory}
import io.circe.syntax.*
import io.circe.literal.*
import io.circe.*

/** Codecs for Blindsight Logging */
object BlindsightCodecs {
  val x                                = 10
//
//  given jsonObject: ToArgument[JsonObject] = ToArgument { (v: JsonObject) =>
//    new Argument(v)
//  }
  given ToArgument[java.nio.file.Path] = ToArgument { path =>
    new Argument(path.toAbsolutePath.toString)
  }

  given ToArgument[Parser.Error] = ToArgument { err =>
    import net.logstash.logback.argument.StructuredArguments.{given, *}
    import io.circe.generic.auto.*
    val foo                          = "steve"
    val fields: List[(String, Json)] = List(
      "at"       -> err.failedAtOffset.asJson,
      "input"    -> err.input.getOrElse("<NoInput>").asJson,
      "offsets"  -> err.offsets.toList.asJson,
      "expected" -> {
        val expects: Json = err.expected.toList.toString.asJson
        expects
      }
    )
    new Argument(raw("parseError", JsonObject.fromIterable(fields).asJson.spaces2))
  }

  def asMarker(str: String): Marker = MarkerFactory.getDetachedMarker(str)

  given toMarkers: ToMarkers[java.nio.file.Path] = ToMarkers { path =>
    Markers(MarkerFactory.getDetachedMarker(path.toString))
  }

}
