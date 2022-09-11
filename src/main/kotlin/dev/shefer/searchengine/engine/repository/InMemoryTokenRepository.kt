package dev.shefer.searchengine.engine.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import dev.shefer.searchengine.engine.dto.FileLocation
import dev.shefer.searchengine.engine.dto.LineLocation
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private typealias LineIndex = MutableMap<Int, MutableList<Int>>
private typealias FileIndex = MutableMap<String, LineIndex>
private typealias DirectoryIndex = MutableMap<String, FileIndex>
private typealias TokenIndex = MutableMap<String, DirectoryIndex>

class InMemoryTokenRepository : TokenRepository {
    private val INDX: TokenIndex = ConcurrentHashMap()

    override fun registerToken(
        token: String,
        directoryPath: String,
        filename: String,
        lineNumber: Int,
        linePosition: Int
    ) {
        INDX
            .getOrPut(token) { ConcurrentHashMap() }
            .getOrPut(directoryPath) { ConcurrentHashMap() }
            .getOrPut(filename) { ConcurrentHashMap() }
            .getOrPut(lineNumber) { CopyOnWriteArrayList() }
            .add(linePosition)
    }

    override fun findLinesByToken(token: String): List<LineLocation> {
        val get = INDX.get(token) ?: emptyMap()
        return get.flatMap { (dirName, files) ->
            files.flatMap { (filename, lines) ->
                val fileLocation = FileLocation(dirName, filename)
                lines.keys.map { lineId ->
                    LineLocation(fileLocation, lineId)
                }
            }
        }
    }

    override fun checkExists(searchCandidate: LineLocation, queryToken: String): Boolean {
        return INDX.get(queryToken)
            ?.get(searchCandidate.fileLocation.directoryPath)
            ?.get(searchCandidate.fileLocation.fileName)
            ?.get(searchCandidate.lineIndex) != null
    }

    override fun flush(directory: String) {
        val dir = File(directory)
        if (!dir.exists()) {
            throw IOException("No such directory $directory")
        }
        val indexFile = dir.resolve("data_index.json")
        if (indexFile.exists()) {
            indexFile.delete()
        }
        indexFile.createNewFile()

        ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .writeValue(indexFile, INDX)
    }

}
