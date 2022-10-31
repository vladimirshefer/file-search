package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.bash.BashExecutor
import dev.shefer.searchengine.bash.process.BashProcess
import dev.shefer.searchengine.bash.process.BashProcessChain
import dev.shefer.searchengine.bash.process.LazyBashProcess
import org.springframework.stereotype.Component
import org.springframework.util.FileSystemUtils
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.math.absoluteValue
import kotlin.random.Random

@Component
class MediaOptimizer {

    fun createThumbnail(source: Path, target: Path): BashProcess {
        val workDir = createTempDirectory()
        val workingFile = workDir.resolve(source.fileName)
        Files.copy(source, workingFile)

        val toJpgProcess = BashExecutor.toJpg(workingFile)

        val resizeDownProcess = LazyBashProcess {
            val result = findResult(workDir, workingFile)
            val MAX_200X200 = 40000
            BashExecutor.resizeDown(result, MAX_200X200)
        }

        val compressProcess = LazyBashProcess {
            val result = findResult(workDir, workingFile)
            BashExecutor.optimizeJpegToMaxSize(result, 100)
                .also { it.onComplete { Files.copy(result, target) } }
        }

        return BashProcessChain.of(
            listOf(
                toJpgProcess,
                resizeDownProcess,
                compressProcess,
            )
        )
            .also { it.onComplete { workDir.toFile().deleteRecursively() } }

    }

    fun optimizeImage(source: Path, target: Path): BashProcess {
        val workDir = createTempDirectory()
        val workingFile = workDir.resolve(source.fileName)
        Files.copy(source, workingFile)
        val toJpgProcess = BashExecutor.toJpg(workingFile)

        val optimizeProcess = LazyBashProcess {
            val result = findResult(workDir, workingFile)
            BashExecutor.optimizeJpeg(result)
        }
        val resizeProcess = LazyBashProcess {
            val result = findResult(workDir, workingFile)
            BashExecutor.resizeDown(result, BashExecutor.FULLHD_PIXELS)
        }
        val compressProcess = LazyBashProcess {
            val result = findResult(workDir, workingFile)
            BashExecutor.optimizeJpegToMaxSize(result, 500)
                .also {
                    it.onComplete {
                        Files.copy(result, target, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
        }
        return BashProcessChain.of(
            listOf(
                toJpgProcess,
                optimizeProcess,
                resizeProcess,
                compressProcess,
            )
        )
            .also { it.onComplete { workDir.toFile().deleteRecursively() } }

    }

    private fun findResult(workDir: Path, workingFile: Path): Path {
        return Files.list(workDir)
            .filter { it.fileName != workingFile.fileName }
            .findAny()
            .orElse(workingFile)
    }

    fun optimizeVideo(source: Path, target: Path): BashProcess {
        val workDir = createTempDirectory()
        val workingFile = workDir.resolve("source").resolve(source.fileName)
        val resultFile = workDir.resolve("result").resolve(source.fileName)
        workDir.resolve("source").createDirectories()
        workDir.resolve("result").createDirectories()
        return LazyBashProcess {
            Files.copy(source, workingFile)
            BashExecutor.toMp4WithQuality28(workingFile, resultFile)
        }
            .also { it.onComplete { Files.copy(resultFile, target) } }
    }

    companion object {
        fun <T : Any?> withTempDirectory(action: (dir: Path) -> T): T {
            val OPEN_DIRECTORY_ON_START = false

            val dir: Path = createTempDirectory()
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

        private fun createTempDirectory(): Path {
            return Files.createTempDirectory(Random.nextInt().absoluteValue.toString())
        }
    }
}
