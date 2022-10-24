package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.bash.BashExecutor
import org.springframework.stereotype.Component
import org.springframework.util.FileSystemUtils
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path
import kotlin.random.Random

@Component
class MediaOptimizer {
    fun optimizeImage(source: Path, target: Path) {
        withTempDirectory { workDir ->
            val workingFile = workDir.resolve(source.fileName)
            Files.copy(source, workingFile)
            var output = ""
            output += BashExecutor.toJpg(workingFile)

            val orElse =
                Files.list(workDir)
                    .filter { it.fileName != workingFile.fileName }
                    .findAny()
                    .orElse(workingFile)

            output += BashExecutor.optimizeJpeg(orElse)
            output += "\n"
            output += BashExecutor.resizeDown(orElse, BashExecutor.FULLHD_PIXELS)
            output += BashExecutor.optimizeJpegToMaxSize(orElse, 500)
            Files.copy(orElse, target)

            output
        }
    }

    fun optimizeVideo(source: Path, target: Path) {
        withTempDirectory { workDir ->
            val workingFile = workDir.resolve("source").resolve(source.fileName)
            val resultFile = workDir.resolve("result").resolve(source.fileName)
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
