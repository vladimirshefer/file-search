package dev.shefer.searchengine.bash

import dev.shefer.searchengine.bash.dto.Resolution
import dev.shefer.searchengine.bash.process.BashProcess
import dev.shefer.searchengine.bash.process.PreparedBashProcess
import dev.shefer.searchengine.bash.process.assertSuccess
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.exists

class BashExecutor {
    companion object {

        private val LOG = LoggerFactory.getLogger(BashExecutor::class.java)

        val FULLHD_PIXELS = 2073600

        /**
         * @param image absolute path to image
         */
        fun toJpg(image: Path): BashProcess {
            return prepareProcessForFile(image, listOf("mogrify", "-monitor", "-format", "jpg"))
        }

        /**
         * @param image absolute path to image
         */
        fun optimizeJpeg(image: Path): BashProcess {
            return prepareProcessForFile(image, listOf("jpegoptim"))
        }

        /**
         * @param image absolute path to image
         */
        fun resizeDown(image: Path, pixelsLimit: Int): BashProcess {
            return prepareProcessForFile(image, listOf("mogrify", "-monitor", "-resize", "$pixelsLimit@>"))
        }

        /**
         * @param image absolute path to image
         * @param maxSizeKb maximum size of output file.
         */
        fun optimizeJpegToMaxSize(image: Path, maxSizeKb: Int): BashProcess {
            return prepareProcessForFile(image, listOf("jpegoptim", "--size=${maxSizeKb}K"))
        }

        fun toMp4WithQuality28(source: Path, target: Path): BashProcess {
            assertFileExists(source)
            return prepareProcess(
                source.parent,
                arrayOf(
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
            )
        }

        fun videoResolution(video: Path): Resolution {
            val bashProcess = prepareProcessForFile(
                video, listOf(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height",
                    "-of", "csv=s=x:p=0"
                )
            )
            val output: String = bashProcess.start().join()
                .assertSuccess()
                .output

            return Resolution(
                output.substringBefore('x').toInt(),
                output.substringAfter('x').substringBefore('\n').toInt()
            )
        }

        private fun prepareProcessForFile(
            image: Path,
            command: List<String>
        ): BashProcess {
            assertFileExists(image)
            return prepareProcess(image.parent, arrayOf(*command.toTypedArray(), image.fileName.toString()))
        }

        private fun assertFileExists(image: Path) {
            if (!image.exists()) {
                throw FileNotFoundException("No such file $image")
            }
        }

        private fun prepareProcess(
            workingDirectory: Path,
            command: Array<String>
        ): BashProcess {
            if (!workingDirectory.exists()) {
                throw FileNotFoundException("No such directory $workingDirectory")
            }

            val processBuilder = ProcessBuilder()
                .directory(workingDirectory.toFile())
                .command(*command)

            return PreparedBashProcess(processBuilder)
        }

    }

}
