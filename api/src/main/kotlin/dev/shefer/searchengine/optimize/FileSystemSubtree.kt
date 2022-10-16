package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.optimize.exceptions.IllegalFileAccessException
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

class FileSystemSubtree(
    private val root: Path
) {

    /**
     *
     * @param path relative path.
     */
    fun listFilesOrEmpty(path: Path): Set<Path> {
        val absolutePath = resolve(path)
        if (!absolutePath.exists()) {
            return emptySet()
        }

        return Files.list(absolutePath)
            .filter { it.isRegularFile() }
            .map { root.relativize(it) }
            .collect(Collectors.toSet())
    }

    /**
     * Returns empty list if directory does not exist in the subtree.
     * Throws an exception if path targets out of root.
     *
     * @param path relative path.
     */
    fun listDirectoriesOrEmpty(path: Path): Set<Path> {
        val absolutePath = resolve(path)
        if (!absolutePath.exists()) {
            return emptySet()
        }

        return Files.list(absolutePath)
            .filter { it.isDirectory() }
            .map { root.relativize(it) }
            .collect(Collectors.toSet())
    }

    /**
     * ```
     * root: /home/user; .resolve("dir") -> "/home/user/dir"
     * root: /home/user; .resolve("../root") -> IllegalFileAccessException
     * root: /home/user; .resolve("/var/log") -> IllegalFileAccessException
     * ```
     *
     * @param path relative path.
     */
    fun resolve(path: Path): Path {
        val result = root.resolve(path).normalize()
        if (!result.startsWith(root)) {
            throw IllegalFileAccessException("Path is out of media root folder: $path")
        }
        return result
    }

    /**
     * ```
     * root: /home/user; .relativize("/home/user/dir") -> "dir"
     * root: /home/user; .relativize("/var/log") -> IllegalFileAccessException
     * ```
     *
     * @param path absolute path.
     */
    fun relativize(path: Path): Path {
        val normalized = path.normalize()
        if (!normalized.startsWith(root)) {
            throw IllegalFileAccessException("Path is out of media root folder: $path")
        }
        return root.relativize(normalized).normalize()
    }

    fun fileSize(filePath: Path): Long {
        return resolve(filePath).fileSize()
    }

}
