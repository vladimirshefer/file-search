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

    /**
     * @param targetDir usually, the test temporary directory.
     * @param testFileName file name of the test resources file
     * @param targetFileName `testFileName` will be renamed to this name in `targetDir`
     * @return the absolute path of created (placed) test file.
     */
    fun placeTestFile(targetDir: Path, testFileName: String, targetFileName: String = testFileName): Path {
        if (!targetDir.exists()) {
            targetDir.createDirectories()
        }
        val testFile = testFile(testFileName)
        val targetFile = targetDir.resolve(targetFileName)
        targetFile.parent.createDirectories()
        Files.copy(testFile, targetFile)
        return targetFile
    }

    /**
     * @param action (absolute path of created temp directory -> expected action with this directory)
     */
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
