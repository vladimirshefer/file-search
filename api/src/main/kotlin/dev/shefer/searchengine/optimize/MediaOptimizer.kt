package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.bash.BashExecutor
import org.springframework.stereotype.Component
import org.springframework.util.FileSystemUtils
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.random.Random

@Component
class MediaOptimizer {

    fun createThumbnail(source: Path, target: Path) {
        withTempDirectory { workDir ->
            val workingFile = workDir.resolve(source.fileName)
            Files.copy(source, workingFile)
            var output = ""
            output += BashExecutor.toJpg(workingFile)

            val result = findResult(workDir, workingFile)

            val MAX_200X200 = 40000
            output += BashExecutor.resizeDown(result, MAX_200X200)
            output += BashExecutor.optimizeJpegToMaxSize(result, 100)
            Files.copy(result, target)

            output
        }
    }

    fun optimizeImage(source: Path, target: Path) {
        withTempDirectory { workDir ->
            val workingFile = workDir.resolve(source.fileName)
            Files.copy(source, workingFile)
            var output = ""
            output += BashExecutor.toJpg(workingFile)

            val result = findResult(workDir, workingFile)

            output += BashExecutor.optimizeJpeg(result)
            output += "\n"
            output += BashExecutor.resizeDown(result, BashExecutor.FULLHD_PIXELS)
            output += BashExecutor.optimizeJpegToMaxSize(result, 500)
            Files.copy(result, target)

            output
        }
    }

    private fun findResult(workDir: Path, workingFile: Path): Path {
        return Files.list(workDir)
            .filter { it.fileName != workingFile.fileName }
            .findAny()
            .orElse(workingFile)
    }

    fun optimizeVideo(source: Path, target: Path) {
        withTempDirectory { workDir ->
            val workingFile = workDir.resolve("source").resolve(source.fileName)
            val resultFile = workDir.resolve("result").resolve(source.fileName)
            workDir.resolve("source").createDirectories()
            workDir.resolve("result").createDirectories()
            Files.copy(source, workingFile)
            val output = BashExecutor.toMp4WithQuality28(workingFile, resultFile)

            Files.copy(resultFile, target)

            output
        }
    }

    companion object {
        fun <T : Any?> withTempDirectory(action: (dir: Path) -> T): T {
            val OPEN_DIRECTORY_ON_START = false

            val dir: Path = Files.createTempDirectory(Random.nextInt().toString())
            try {
                if (OPEN_DIRECTORY_ON_START) {
                    Desktop.getDesktop().open(dir.toFile())
                }
                return action(dir)
            } finally {
                if (dir.toFile().exists()) {
                    FileSystemUtils.deleteRecursively(dir)
                }
            }
        }
    }
}
