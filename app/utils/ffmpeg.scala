package utils

import collection.mutable.ListBuffer
import java.io.File
import play.api.Logger
import collection.mutable
import play.api.libs.Files

case class Error(kind: String)

case class Passed(kind: String)

case class ffmpeg(file: File) {
  private lazy val logger = Logger("ffmpeg")
  private val Duration = """Duration:\s+(\d+):(\d+):(\d+)\.(\d+).+""".r
  private val MediaType = """Input #0, (\w+),.""".r
  private val Encoding = """Stream #0:0: Audio: ([^,]+), (\d+) Hz.+""".r
  private val Sample = """Stream.(\d+) Hz"""
  private val base = Seq("ffmpeg", "-i", file.getAbsolutePath)
  private val SUPPORTED_TYPES = List("flac", "wav", "aiff")
  private val SUPPORTED_BITS = List("16", "24")
  private val SUPPORTED_SAMPLES = List("44100", "48000", "88200", "96000", "176400", "192000")
  private val BASE_ENCODING_ARGS = Seq("-ar 44100", "-ab 128k")

  def encode(output: File, length: Int = 0) = {


    // if the output file already exists then create a temp file for encoding just in case
    // the encoding fails
    val maybeAdjustedOutput = if (output.exists()) new File(output.getParentFile, "temp_" + output.getName) else output
    var args = Seq("-ar", "44100", "-ab", "128k")

    if (length > 0) args ++= Seq("-t", length.toString)
    args ++= Seq(maybeAdjustedOutput.getAbsolutePath)

    val (exitCode, buffer) = command(args)
    // if the encoding was OK then check if we encoded the temp file, if so replace it with the correct output
    if (exitCode == 0 && !maybeAdjustedOutput.equals(output)) Files.moveFile(maybeAdjustedOutput, output, true)

    exitCode == 0


  }


  def verify = {
    val (exitCode, buffer) = command(Seq.empty[String])


    buffer.toList.map {


      _.trim match {
        case Duration(hours, mintues, seconds, mills) => if (mintues.toInt < 1) "duration"

        case Encoding(mediaType, sample) => {
          Logger.debug(mediaType + " :: " + sample)
          if (!SUPPORTED_TYPES.contains(mediaType)) "media"
          if (!SUPPORTED_SAMPLES.contains(sample)) "sample"
        }
        case _ => ()
      }

    }.filter(_.isInstanceOf[String])

  }

  lazy val duration = {
    val (exitCode, buffer) = command(Seq.empty[String])

    buffer.toList.map {
      line =>
        line.trim match {
          case Duration(hours, minutes, seconds, mills) => Some((hours.toInt * 60 * 60) + (minutes.toInt * 60) + seconds.toInt)
          case _ => None
        }
    }.filter(_.isDefined).head.getOrElse(0)
  }

  def command(args: Seq[String]): (Int, ListBuffer[String]) = {
    import scala.sys.process._

    val buffer = new scala.collection.mutable.ListBuffer[String]

    val logger = ProcessLogger((line: String) => buffer append (line))
    val command = (base ++ args).mkString(" ")
    val exitCode = (base ++ args).!(logger)

    (exitCode, buffer)
  }
}



