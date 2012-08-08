package utils

import org.apache.commons.codec.digest.DigestUtils._
import play.api.{Logger, Play}
import java.io.{FileFilter, File}
import play.api.libs.Files.TemporaryFile
import javax.imageio.{IIOImage, ImageIO}
import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.stream.FileImageOutputStream
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files

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


abstract class Image(_id: String, imageSize: ImageSize = Normal(), tempFile: Option[FilePart[TemporaryFile]] = None) extends DataStore {
  override def location(): String = "images"

  def id: String = _id

  def filename(): String = String.format("%s_%s.jpg", id, imageSize.suffix)

  lazy val host = config.getString("datastore.art.host").get

  protected val key = _id

  def dir: String = {


    if (shard) key.substring(0, 5).toCharArray.mkString("/") else ""


  }

  def toDir(): File = new File(store, dir)

  def uri: String = "/media/images/" + path

  lazy val path = dir + filename

  def toFile(): File = new File(store, path)


  def url: String = {
    validate()
    Some(exists).map {
      case true => "/assets" + uri
      case _ => ""

    }.getOrElse("")


  }

  def validate() {

    tempFile.map(_.ref.moveTo(toFile, replace = true))
  }

  def exists: Boolean = {
    toFile().exists()
  }

  def getOrResize(size: ImageSize): Image = {
    val image = BaseImage(id, size)
    if (image.exists) image else resizeTo(size)
  }

  def resizeTo(size: ImageSize): Image = {
    validate()
    val out = BaseImage(id, size)
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
  def resize(originalImage: File, to: File, w: Int, h: Int): Boolean = {
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
  def resize(originalImage: File, to: File, width: Int, height: Int, keepRatio: Boolean): Boolean = {
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

case class BaseImage(_id: String, imageSize: ImageSize = Normal(), tempFile: Option[FilePart[TemporaryFile]] = None) extends Image(_id, imageSize, tempFile) {


}


case class TempImageDataStore(session: String) extends DataStore {
  override def location(): String = "images"

  lazy val path = "temp/" + session

  def toDir(): File = new File(store, path)

  private val noopFileFilter = new FileFilter {
    def accept(file: File) = true
  }

  def commit(filter: Option[FileFilter]) = {
    if (toDir().exists()) {
      toDir().listFiles(new FileFilter {
        def accept(file: File) = filter.getOrElse(noopFileFilter).accept(file) && file.getName.contains("_normal")
      }).map {
        tempFile => {
          val id = tempFile.getName.split("_")(0)


          val filePart = FilePart(tempFile.getName, tempFile.getName, None, TemporaryFile(tempFile))

          BaseImage(id, Normal(), Some(filePart)).validate()
        }

      }
      toDir().delete()
    }

  }
}

case class TempImage(_id: String, session: String, imageSize: ImageSize = Normal(), tempFile: Option[FilePart[TemporaryFile]] = None) extends Image(_id, imageSize, tempFile) {

  override lazy val path = "temp/" + session + "/" + filename

  override def resizeTo(size: ImageSize): Image = {
    validate()
    val out = TempImage(id, session, size, None)
    out.toFile().getParentFile.mkdirs();
    resize(toFile, out.toFile, size.width, size.height, true)

    out
  }
}


object TempImage {
  def apply(id: String, session: String) = new TempImage(id, session)

  def apply(id: String, session: String, file: FilePart[TemporaryFile]) = new TempImage(id, session, Normal(), Some(file))

  def apply(file: FilePart[TemporaryFile], session: String) = {
    val id = shaHex(System.nanoTime() + file.filename)
    new TempImage(id, session, Normal(), Some(file))
  }

  def commitImagesForSession(session: String, filter: Option[FileFilter]) = TempImageDataStore(session).commit(filter)


}

object Image {

  /* def fromTemp(id: String): Image = {
    val tempFile = TempImage(id).toFile()
    val filePart = FilePart(tempFile.getName, tempFile.getName, None, TemporaryFile(tempFile))
    return BaseImage(id, Normal(), Some(filePart))
  }*/

  def apply(id: String) = new BaseImage(id)

  def apply(id: String, imageSize: ImageSize) = new BaseImage(id, imageSize)

  def apply(file: FilePart[TemporaryFile]) = {
    val id = shaHex(System.nanoTime() + file.filename)
    new BaseImage(id, Normal(), Some(file))
  }

  def size(file: File) = {
    val source = ImageIO.read(file);
    (source.getWidth, source.getHeight)

  }


}


class AudioDataStore extends DataStore {
  override def location(): String = "audio"

  import java.io.File

  def album(folder: String) = {
    val dir = if (shard) folder.substring(0, 5).toCharArray.mkString("/") else ""
    new File(store, dir + folder)

  }

  def zipFile(kind: String, id: String) = new File(temp, "zips/" + kind + "/" + id + ".zip")

  def zip(kind: String, id: String) = {
    val file = zipFile(kind, id)
    if (file.exists()) Some(file) else None
  }

  def asZip(output: File, files: Iterable[(String, File)])(setter: (Int, String) => String) = {
    import java.io.{BufferedInputStream, FileInputStream, FileOutputStream}
    import java.util.zip.{ZipEntry, ZipOutputStream}
    output.getParentFile.mkdirs()
    val zip = new ZipOutputStream(new FileOutputStream(output))
    zip.setLevel(6)


    for (((name, file), index) <- files.zipWithIndex) {


      val b = new Array[Byte](1024)
      val entry = new ZipEntry(setter(index + 1, name))
      val in = new BufferedInputStream(new FileInputStream(file))
      entry.setSize(file.length())

      zip.putNextEntry(entry)


      var count = in.read(b)
      while (count > 0) {

        zip.write(b, 0, count)
        count = in.read(b)
      }

      in.close()
      zip.closeEntry()
    }
    zip.close()


  }

  override def toRoot(session: String) = album(session)

  override def toDir(session: String) = album(session)

  def toURL(host: String, file: File) = ("http://" + host + "/" + httpPath + file.getAbsolutePath.replace(store.getAbsolutePath, "")).replace("\\", "/")

  def previewURL(host: String, session: String, file: String) = toURL(host, preview(album(session), file))

  def fullURL(host: String, session: String, file: String) = toURL(host, full(album(session), file))

  def preview(album: File, file: String) = new File(album, file + "_preview.mp3")

  def full(album: File, file: String) = new File(album, file + "_full.mp3")

  def full(album: String, file: String) = new File(toDir(album), file + "_full.mp3")

  def fullName(file: String) = file + "_full.mp3"


}

object Assets {

  lazy val audioStore = new AudioDataStore()
  lazy val tempAudioStore = new TempAudioDataStore()

  def deleteDirectory(file: File): Unit = {
    if (file.exists()) {
      file.listFiles().foreach {
        f =>
          if (f.isDirectory()) deleteDirectory(f)
          f.delete()
      }

      file.delete()
    }
  }
}

object AudioDataStore {


  def deleteAudioSession(session: String) = {
    Assets.deleteDirectory(Assets.audioStore.album(session))

  }
}

class TempAudioDataStore extends AudioDataStore {

  override lazy val store = temp


}

object TempAudioDataStore {


  import DataStore.transferToDataStore

  def commitAudioForSession(session: String, filter: Option[FileFilter]) = transferToDataStore(Assets.tempAudioStore, Assets.audioStore, session, filter)


}

object DataStore {
  private val noopFileFilter = new FileFilter {
    def accept(file: File) = true
  }

  def transferToDataStore(from: DataStore, to: DataStore, session: String, filter: Option[FileFilter]) = {
    val root = from.toDir(session)
    if (root.exists()) {
      root.listFiles(filter.getOrElse(noopFileFilter)).map {
        file =>
          Logger.debug("Moving %s to %s" format(file.getAbsolutePath, to.toFile(session, file.getName)))
          Files.moveFile(file, to.toFile(session, file.getName), true)
      }
      if (root.list().isEmpty) root.delete()
    }
  }
}

trait DataStore {

  def location(): String = ""

  import play.api.Play.current


  lazy val app = Play.application
  lazy val config = app.configuration

  protected def _s(name: String) = {
    config.getString("datastore." + location + "." + name).get
  }

  lazy val httpPath = _s("path")
  lazy val temp = app.getFile(_s("temp"))
  lazy val fragment = _s("location")
  lazy val store = app.getFile(fragment)
  lazy val shard = _s("shard").equals("true")

  protected def ext(file: String): String = {
    val index = file.lastIndexOf(".");

    file.substring(index + 1, file.length());
  }

  protected def composeFile(base: File, path: String) = new File(base, path)

  def tempFile(name: String): Option[File] = {
    val f = new File(temp, name)
    if (f.exists()) Some(f) else None
  }

  def tempFile(name: String, session: String): Option[File] = {
    val f = new File(temp, session + "/" + name)
    if (f.exists()) Some(f) else None
  }

  def toRoot(session: String) = new File(store, session)


  def toDir(session: String) = toRoot(session)

  def toFile(session: String, file: String) = new File(toDir(session), file)

  def tempExists(name: String, session: String) = new File(temp, session + "/" + name).exists()

  def toTempMaybeId(id: Option[String], file: FilePart[TemporaryFile], session: String, withExt: Boolean = true): (Boolean, String, File) = {
    val name = (if (id.isDefined) id.get else shaHex(System.currentTimeMillis().toString())) + (if (withExt) ("." + ext(file.filename)) else "")
    val tempFile = new File(temp, session + "/" + name);


    //Logger.debug(tempFile.getAbsolutePath)
    file.ref.moveTo(tempFile, true)
    return (tempFile.exists(), tempFile.getName, tempFile)


  }

  def tempExists(name: String) = new File(temp, name).exists()

  def moveToTemp(file: FilePart[TemporaryFile], withExt: Boolean = true): (Boolean, String, File) = {
    val name = shaHex(System.currentTimeMillis().toString()) + (if (withExt) ("." + ext(file.filename)) else "")
    val tempFile = new File(temp, name);


    //Logger.debug(tempFile.getAbsolutePath)
    file.ref.moveTo(tempFile, true)
    return (tempFile.exists(), tempFile.getName, tempFile)


  }


}
