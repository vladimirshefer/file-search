package dev.shefer.searchengine.util

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

object FileUtil {
    fun forEachAccessibleFile(path: Path, action: (Path, BasicFileAttributes) -> Unit) {
        Files.walkFileTree(path, object : SimpleFileVisitor<Path?>() {
            override fun visitFileFailed(file: Path?, e: IOException?): FileVisitResult {
                return FileVisitResult.SKIP_SUBTREE
            }

            override fun visitFile(file: Path?, attrs: BasicFileAttributes): FileVisitResult {
                val result = super.visitFile(file, attrs)

                file ?: return result

                action(file, attrs)

                return result
            }
        })
    }
}
