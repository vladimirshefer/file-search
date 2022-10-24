package dev.shefer.searchengine.bash

import dev.shefer.searchengine.bash.dto.Resolution
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.exists

class BashExecutor {
    companion object {

        private val LOG = LoggerFactory.getLogger(BashExecutor::class.java);

        val FULLHD_PIXELS = 2073600

        /**
         * @param image absolute path to image
         */
        fun toJpg(image: Path): String {
            return executeForFile(image, listOf("mogrify", "-monitor", "-format", "jpg"))
                .also { LOG.info(it) }
        }

        /**
         * @param image absolute path to image
         */
        fun optimizeJpeg(image: Path): String {
            return executeForFile(image, listOf("jpegoptim"))
                .also { LOG.info(it) }
        }

        /**
         * @param image absolute path to image
         */
        fun resizeDown(image: Path, pixelsLimit: Int): String {
            return executeForFile(image, listOf("mogrify", "-monitor", "-resize", "$pixelsLimit@>"))
                .also { LOG.info(it) }
        }

        /**
         * @param image absolute path to image
         * @param maxSizeKb maximum size of output file.
         */
        fun optimizeJpegToMaxSize(image: Path, maxSizeKb: Int): String {
            return executeForFile(image, listOf("jpegoptim", "--size=${maxSizeKb}K"))
                .also { LOG.info(it) }
        }

        fun toMp4WithQuality28(source: Path, target: Path): String {
            assertFileExists(source)
            return execute(
                source.parent,
                "ffmpeg",
                "-n",
                "-i",
                source.toString(),
                "-c:v",
                "libx265",
                "-crf",
                "28",
                "-c:a",
                "copy",
                target.toString()
            )
        }

        fun videoResolution(video: Path): Resolution {
            val output: String = executeForFile(
                video,
                listOf(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height",
                    "-of", "csv=s=x:p=0"
                )
            )
                .also { LOG.info(it) }

            return Resolution(
                output.substringBefore('x').toInt(),
                output.substringAfter('x').substringBefore('\n').toInt()
            )
        }

        private fun assertFileExists(image: Path) {
            if (!image.exists()) {
                throw FileNotFoundException("No such file $image")
            }
        }

        private fun executeForFile(path: Path, command: List<String>): String {
            assertFileExists(path)

            return execute(path.parent, *command.toTypedArray())
        }

        private fun execute(workingDirectory: Path, vararg command: String): String {
            if (!workingDirectory.exists()) {
                throw FileNotFoundException("No such directory $workingDirectory")
            }

            val process = ProcessBuilder()
                .directory(workingDirectory.toFile())
                .command(*command)
                .start()

            val statusCode = process.waitFor()
            val output = process.inputReader().readText()
            val error = process.errorReader().readText()
            if (statusCode != 0) {
                throw RuntimeException("Could not execute bash command. Status: $statusCode. Error: $error")
            }

            return output
        }

    }

}
