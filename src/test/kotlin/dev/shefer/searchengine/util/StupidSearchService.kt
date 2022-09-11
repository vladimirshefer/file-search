package dev.shefer.searchengine.util

import dev.shefer.searchengine.engine.dto.FileLocation
import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.search.SearchService
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.io.path.readLines

/**
 * Simple service which performs text search in directory in naive way.
 */
class StupidSearchService(
    private val sourceDir: String
) : SearchService {

    override fun search(query: String): List<LineLocation> {
        val searchResults = ArrayList<LineLocation>()
        forEachFile(sourceDir) { file -> searchResults.addAll(findInFile(file, query)) }
        return searchResults
    }

    companion object {
        fun forEachFile(
            directory: String,
            block: (Path) -> Unit
        ) {
            Files.walkFileTree(Paths.get(directory), SearchingFileVisitor(block))
        }

        class SearchingFileVisitor(
            val function: (Path) -> Unit
        ) : SimpleFileVisitor<Path?>() {
            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                val result = super.visitFile(file, attrs)
                if (file == null) {
                    return result
                }
                function(file)
                return result
            }
        }

        fun findInFile(file: Path, queryString: String): List<LineLocation> {
            val searchResults = ArrayList<LineLocation>()
            file.readLines().forEachIndexed { i, line ->
                if (line.contains(queryString)) {
                    searchResults.add(
                        LineLocation(
                            FileLocation(
                                file.parent.absolutePathString(),
                                file.name
                            ),
                            i
                        )
                    )
                }
            }
            return searchResults
        }
    }
}
