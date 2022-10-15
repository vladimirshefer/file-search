package dev.shefer.test_internal

import org.junit.jupiter.api.Assertions
import org.springframework.util.FileSystemUtils
import org.springframework.util.ResourceUtils
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readBytes

object TestFilesUtil {

    fun assertFilesEquals(expectedImg: Path, actualImg: Path) {
        Assertions.assertArrayEquals(expectedImg.readBytes(), actualImg.readBytes())
    }

    fun resource(path: String): Path {
        return ResourceUtils.getFile(path).toPath()
    }

    fun testFile(name: String): Path {
        return resource("./src/test/resources/test_files/$name").normalize()
    }

    fun placeTestFile(dir: Path, testFileName: String, targetFileName: String = testFileName) {
        if (!dir.exists()) {
            dir.createDirectories()
        }
        val testFile = testFile(testFileName)
        val targetFile = dir.resolve(targetFileName)
        targetFile.parent.createDirectories()
        Files.copy(testFile, targetFile)
    }

    fun withTempDirectory(action: (dir: Path) -> Unit) {
        val OPEN_DIRECTORY_ON_START = false

        val dir: Path = Files.createTempDirectory(this.javaClass.simpleName)
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
