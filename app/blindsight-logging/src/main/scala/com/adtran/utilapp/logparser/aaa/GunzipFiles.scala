package com.adtran.utilapp.logparser.aaa

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.data.Validated.{*, given}

import scala.util.chaining.*
import java.nio.file.Path
import _root_.fs2.io.{file, *}
import _root_.fs2.io.file.Files
import com.adtran.utilapp.logparser.aaa.pipelines.GunzipPipeline
import org.log4s.Logger

/** Old Unix process style -- deprecated */
object GunzipFiles {
  val logger: Logger = org.log4s.getLogger

  /** An check that returns ValidatedNel[String,Unit] for edge case */
  private def ioBoolValidated(t: IO[Boolean], error: String): IO[ValidatedNel[String, Unit]] = {
    t.map(b => condNel(b, (), error))
  }

  private def allValidIO[T](target: T, check: IO[ValidatedNel[String, Unit]]*): IO[ValidatedNel[String, T]] = {
    check.sequence.map(_.combineAll.map(_ => target))
  }

  private def validNelThrown[T](v: ValidatedNel[String, T]): IO[T] =
    val ev: Either[IllegalArgumentException, T] = v.toEither
      .leftMap(v => new IllegalArgumentException(v.mkString_(";")))
    logger.warn(s"Validated: $ev")
    IO.fromEither(ev)

  private def validateDir(dir: file.Path): IO[file.Path] = {
    allValidIO(
      dir,
      ioBoolValidated(Files[IO].exists(dir), s"Directory $dir didn't exist"),
      ioBoolValidated(Files[IO].isReadable(dir), s"Directory $dir not readable"),
      ioBoolValidated(Files[IO].isExecutable(dir), s"Directory $dir not executable")
    ).flatMap((vnel: ValidatedNel[String, file.Path]) => validNelThrown(vnel))
      .flatTap(x => IO(logger.info(s"Validated Source Dir: $x")))

  }
  def unCompressFilesIn(dir: file.Path, withPrefix: String): IO[Unit] = {
    logger.info(s"UnPacking Files in $dir")

    /** Unzips all the .gz files matching withPrefix into same directory */
    def gunzipStream(dir: file.Path): fs2.Stream[IO, file.Path] =
      Files[IO]
        .list(dir)
        .filter((f: file.Path) => f.extName == ".gz" && f.fileName.toString.startsWith(withPrefix))
        .evalFilter(f => Files[IO].isRegularFile(f))
        .map(inF => (inF, dir.resolve(inF.fileName.toString.dropRight(3))))
        .evalFilterNot((in: file.Path, out: file.Path) => Files[IO].exists(out))
        .through(GunzipPipeline.gunzipBinPipe)

    validateDir(dir)
      .map((dir: file.Path) => gunzipStream(dir))
      .flatMap((stream) => stream.compile.drain)
  }

}
