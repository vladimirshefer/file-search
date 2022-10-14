package dev.shefer.searchengine.bash

import dev.shefer.searchengine.bash.dto.Resolution
import java.io.FileNotFoundException
import java.nio.file.Path

class BashExecutor {
    companion object {
        val FULLHD_PIXELS = 2073600

        fun toJpg(image: Path) {
            assertFileExists(image)

            val process = ProcessBuilder()
                .directory(image.parent.toFile())
                .command("mogrify", "-format", "jpg", image.fileName.toString())
                .start()
                .also { it.waitFor() }

            println(process.inputReader().readText())

        }

        fun optimizeJpeg(image: Path) {
            assertFileExists(image)

            ProcessBuilder()
                .directory(image.parent.toFile())
                .command("jpegoptim", image.fileName.toString())
                .start()
                .waitFor()
        }

        fun resizeDown(image: Path, pixelsLimit: Int) {
            assertFileExists(image)

            ProcessBuilder()
                .directory(image.parent.toFile())
                .command("mogrify", "-resize", "$pixelsLimit@>", image.fileName.toString())
                .start()
                .waitFor()
        }

        fun videoResolution(video: Path): Resolution {
            assertFileExists(video)
            val start = ProcessBuilder()
                .directory(video.parent.toFile())
                .command(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height",
                    "-of", "csv=s=x:p=0", video.fileName.toString()
                )
                .start()

            val statusCode = start.waitFor()

            if (statusCode != 0) {
                val errorText = start.errorReader().use { it.readText() }
                throw RuntimeException("Could not execute bash command. Status: $statusCode. Error: $errorText")
            }

            val output: String = start.inputReader().use { it.readText() }
            return Resolution(
                output.substringBefore('x').toInt(),
                output.substringAfter('x').substringBefore('\n').toInt()
            )
        }

        private fun assertFileExists(image: Path) {
            if (image.toFile().exists().not()) {
                throw FileNotFoundException("No such file $image")
            }
        }

    }

}
