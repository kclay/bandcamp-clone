package utils

import org.apache.commons.codec.digest.DigestUtils._
import play.api.{Logger, Play}
import java.io.File
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import javax.imageio.{IIOImage, ImageIO}
import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.stream.FileImageOutputStream
import play.api.mvc.MultipartFormData.FilePart

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/20/12
 * Time: 10:42 AM
 */

sealed abstract class ImageSize(val width: Int, val height: Int, val suffix: String)

case class Normal() extends ImageSize(350, 350, "normal")

case class Medium() extends ImageSize(210, 210, "medium")

case class Small() extends ImageSize(72, 72, "small")

case class Image(id: String, imageSize: ImageSize = Normal(), tempFile: Option[FilePart[TemporaryFile]] = None) extends DataStore
{

  override def location(): String = "images"


  def filename(): String = String.format("%s_%s.jpg", id, imageSize.suffix)

  lazy val host = config.getString("datastore.art.host").get


  def dir(): String =
  {


    id.substring(0, 5).toCharArray.mkString("/")


  }

  def uri(): String = "/media/images/" + path

  lazy val path = dir + filename

  def toFile(): File = new File(store, path)


  def url(): String =
  {

    import controllers.routes
    validate()
    "/assets" + uri()

  }

  def validate()
  {

    tempFile.map(_.ref.moveTo(toFile, replace = true))
  }

  def exists(): Boolean =
  {
    toFile().exists()
  }

  def resizeTo(size: ImageSize): Image =
  {
    validate()
    val out = Image(id, size)
    out.toFile().getParentFile.mkdirs();
    resize(toFile, out.toFile, size.width, size.height, true)

    out

  }


  /**
   * Resize an image
   * @param originalImage The image file
   * @param to The destination file
   * @param w The new width (or -1 to proportionally resize)
   * @param h The new height (or -1 to proportionally resize)
   */
  def resize(originalImage: File, to: File, w: Int, h: Int): Boolean =
  {
    resize(originalImage, to, w, h, false);
  }

  /**
   * Resize an image
   * @param originalImage The image file
   * @param to The destination file
   * @param width The new width (or -1 to proportionally resize) or the maxWidth if keepRatio is true
   * @param height The new height (or -1 to proportionally resize) or the maxHeight if keepRatio is true
   * @param keepRatio : if true, resize will keep the original image ratio and use w and h as max dimensions
   */
  def resize(originalImage: File, to: File, width: Int, height: Int, keepRatio: Boolean): Boolean =
  {
    try {
      val source = ImageIO.read(originalImage);
      val owidth = source.getWidth();
      val oheight = source.getHeight();
      val ratio: Double = owidth / oheight;
      var w = width;
      var h = height;
      val maxWidth = w;
      val maxHeight = h;


      if (w < 0 && h < 0) {
        w = owidth;
        h = oheight;
      }
      if (w < 0 && h > 0) {
        w = (h * ratio).toInt
      }
      if (w > 0 && h < 0) {
        h = (w / ratio).toInt
      }

      if (keepRatio) {
        h = (w / ratio).toInt
        if (h > maxHeight) {
          h = maxHeight;
          w = (h * ratio).toInt
        }
        if (w > maxWidth) {
          w = maxWidth;
          h = (w / ratio).toInt
        }
      }


      val mimeType = "image/jpeg";
      /*if (to.getName().endsWith(".png")) {
       mimeType = "image/png";
     }
     if (to.getName().endsWith(".gif")) {
       mimeType = "image/gif";
     } */

      // out
      val dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      val srcSized = source.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
      val graphics = dest.getGraphics()
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, w, h);
      graphics.drawImage(srcSized, 0, 0, null);
      val writer = ImageIO.getImageWritersByMIMEType(mimeType).next();
      val params = writer.getDefaultWriteParam();
      val toFs = new FileImageOutputStream(to);
      writer.setOutput(toFs);
      val image = new IIOImage(dest, null, null);
      writer.write(null, image, params);
      toFs.flush();
      toFs.close();
      true
    } catch {
      case e: Exception => false

    }


  }
}

object Image
{


  def apply(id: String) = new Image(id)

  def apply(file: FilePart[TemporaryFile]) =
  {
    val id = sha256Hex(System.nanoTime() + file.filename)
    new Image(id, Normal(), Some(file))
  }


}

case class AudioDataStore(name: String = "audio") extends DataStore

trait DataStore
{

  def location(): String = ""

  import play.api.Play.current


  lazy val app = Play.application
  lazy val config = app.configuration
  lazy val temp = app.getFile(config.getString("datastore." + location + ".temp").get)
  lazy val fragment = config.getString("datastore." + location + ".location").get
  lazy val store = app.getFile(fragment)

  protected def ext(file: String): String =
  {
    val index = file.lastIndexOf(".");

    file.substring(index + 1, file.length());
  }

  def moveToTemp(file: FilePart[TemporaryFile]): (Boolean, String) =
  {

    /* val tempFile = File.createTempFile(name, "." + ext(file.filename))


 Logger.debug(tempFile.getAbsolutePath)
 file.ref.moveTo(tempFile, true)
 return (tempFile.exists(), tempFile.getName) */
    return (true, "")


  }
}
