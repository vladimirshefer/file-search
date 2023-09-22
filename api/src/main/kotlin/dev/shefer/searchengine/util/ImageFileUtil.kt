package dev.shefer.searchengine.util

import dev.shefer.searchengine.util.ContentTypeUtil.mimeType
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.io.File
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.FileImageInputStream
import javax.imageio.stream.ImageInputStream


object ImageFileUtil {

    private val LOG = LoggerFactory.getLogger(this::class.java)

    val Path.isImage: Boolean get() =  this.mimeType.type == "image"

    val Path.imageResolution: Dimension get() = if (isImage) {
        getImageDimension(this.toFile())
    } else throw IllegalArgumentException("File is not an image $this")

    /**
     * Gets image dimensions for given file
     * @param imgFile image file
     * @return dimensions of image
     * @throws IOException if the file is not a known image
     */
    @Throws(IOException::class)
    private fun getImageDimension(imgFile: File): Dimension {
        val pos: Int = imgFile.getName().lastIndexOf(".")
        if (pos == -1) throw IOException("No extension for file: " + imgFile.getAbsolutePath())
        val suffix: String = imgFile.getName().substring(pos + 1)
        val iter: Iterator<ImageReader> = ImageIO.getImageReadersBySuffix(suffix)
        while (iter.hasNext()) {
            val reader: ImageReader = iter.next()
            try {
                val stream: ImageInputStream = FileImageInputStream(imgFile)
                reader.setInput(stream)
                val width: Int = reader.getWidth(reader.getMinIndex())
                val height: Int = reader.getHeight(reader.getMinIndex())
                return Dimension(width, height)
            } catch (e: IOException) {
                LOG.warn("Error reading: " + imgFile.getAbsolutePath(), e)
            } finally {
                reader.dispose()
            }
        }
        throw IOException("Not a known image file: " + imgFile.getAbsolutePath())
    }
}
