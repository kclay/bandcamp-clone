package tasks

import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/27/12
 * Time: 10:24 AM
 */

object ffmpeg {

  private val ffmpegDuration = """Duration:\s+([\d:\.]+)""".r

  def duration(file: File) {
    val ffmpegDuration(duration) =
      ffmpeg(file.getAbsolutePath)

  }

  private val base = Seq("ffmpeg", "-i")

  def apply(args: String*) {
    import scala.sys.process._
    import scala.sys.process.ProcessLogger;
    val buffer = new StringBuilder();
    //val exitCode = (base ++ args).! ProcessLogger (line => buffer.append(line))


  }
}

object Tasks {


  def encodeToMp3(from: File) {


  }


}
