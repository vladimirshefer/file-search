package dev.shefer.searchengine.bash

import dev.shefer.searchengine.bash.dto.Resolution
import java.io.FileNotFoundException
import java.nio.file.Path

class BashExecutor {
    companion object {
        val FULLHD_PIXELS = 2073600

        fun toJpg(image: Path) {
            println(executeForFile(image, listOf("mogrify", "-format", "jpg")))
        }

        fun optimizeJpeg(image: Path) {
            println(executeForFile(image, listOf("jpegoptim")))
        }

        fun resizeDown(image: Path, pixelsLimit: Int) {
            println(executeForFile(image, listOf("mogrify", "-resize", "$pixelsLimit@>")))
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

        private fun executeForFile(path: Path, command: List<String>): String {
            assertFileExists(path)

            val process = ProcessBuilder()
                .directory(path.parent.toFile())
                .command(command + path.fileName.toString())
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
