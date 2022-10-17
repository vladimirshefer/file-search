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
    fun optimize(source: Path, target: Path) {
        withTempDirectory { workDir ->
            val workingFile = workDir.resolve(source.fileName)
            Files.copy(source, workingFile)
            BashExecutor.toJpg(workingFile)

            val orElse =
                Files.list(workDir)
                    .filter { it.fileName != workingFile.fileName }
                    .findAny()
                    .orElse(workingFile)

            BashExecutor.optimizeJpeg(orElse)
            BashExecutor.resizeDown(orElse, BashExecutor.FULLHD_PIXELS)
            BashExecutor.optimizeJpegToMaxSize(orElse, 500)
            Files.copy(orElse, target)
        }
    }

    companion object {
        fun withTempDirectory(action: (dir: Path) -> Unit) {
            val OPEN_DIRECTORY_ON_START = false

            val dir: Path = Files.createTempDirectory(Random.nextInt().toString())
            try {
                if (OPEN_DIRECTORY_ON_START) {
                    Desktop.getDesktop().open(dir.toFile())
                }
                action(dir)
            } finally {
                if (dir.toFile().exists()) {
                    FileSystemUtils.deleteRecursively(dir)
                }
            }
        }
    }
}
