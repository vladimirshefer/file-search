package dev.shefer.searchengine.files

import dev.shefer.searchengine.files.dto.DirectoryInfo
import dev.shefer.searchengine.files.dto.FileInfo
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.name

/**
 * Utils for interaction with filesystem
 */
class FileAccessor {

    companion object {

        val EXTENSION_WHITELIST = listOf(".kt", ".kts", ".gitignore", ".txt", ".properties", ".bat", ".yaml", ".yml")

        /**
         * Reads the turns the line of the file.
         * TODO: Optimize reads. Now is impossible without file line separators index.
         */
        fun getLine(path: Path, lineIndex: Int): String {
            val filename = path.toString()
            val channel = RandomAccessFile(filename, "r")
            for (i in 0 until lineIndex) {
                channel.readLine()
            }
            return channel.readLine()
                ?: throw IllegalStateException("Could not read line from file. Has the file been changed or deleted?")
        }

        /**
         * Returns list of all accessible files with sizes in this directory and subdirectories.
         */
        fun getDirectoryInfo(directory: Path): DirectoryInfo {
            val fileList = ArrayList<FileInfo>()
            var totalSize = 0L
            Files.walkFileTree(directory.normalize(), object : SimpleFileVisitor<Path?>() {
                override fun visitFileFailed(file: Path?, e: IOException?): FileVisitResult {
                    System.err.printf("Visiting failed for %s\n", file)
                    return FileVisitResult.SKIP_SUBTREE
                }

                override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                    val result = super.visitFile(file, attrs)
                    if (file == null) return result

                    val fileSize = attrs?.size() ?: 0

                    if (EXTENSION_WHITELIST.any { file.name.endsWith(it) }) {
                        totalSize += fileSize
                        fileList.add(FileInfo(file, fileSize))
                    }

                    return result
                }
            })

            return DirectoryInfo(fileList, totalSize)
        }

    }

}
