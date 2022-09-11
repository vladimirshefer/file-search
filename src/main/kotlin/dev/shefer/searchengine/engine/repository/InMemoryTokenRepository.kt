package dev.shefer.searchengine.engine.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import dev.shefer.searchengine.engine.dto.FileLocation
import dev.shefer.searchengine.engine.dto.LineLocation
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

private typealias LineIndex = MutableMap<Int, MutableList<Int>>
private typealias FileIndex = MutableMap<String, LineIndex>
private typealias DirectoryIndex = MutableMap<String, FileIndex>
private typealias TokenIndex = MutableMap<String, DirectoryIndex>

class InMemoryTokenRepository : TokenRepository {

    private val rwLock = ReentrantReadWriteLock()
    private val rLock = rwLock.readLock()
    private val wLock = rwLock.writeLock()

    private var index: TokenIndex = HashMap()

    override fun registerToken(
        token: String,
        directoryPath: String,
        filename: String,
        lineNumber: Int,
        linePosition: Int
    ) {
        writeLock {
            index
                .getOrPut(token) { HashMap() }
                .getOrPut(directoryPath) { HashMap() }
                .getOrPut(filename) { HashMap() }
                .getOrPut(lineNumber) { ArrayList() }
                .add(linePosition)
        }
    }

    override fun findLinesByToken(token: String): List<LineLocation> {
        return readLock {
            val get = index.get(token) ?: emptyMap()
            get.flatMap { (dirName, files) ->
                files.flatMap { (filename, lines) ->
                    val fileLocation = FileLocation(dirName, filename)
                    lines.keys.map { lineId ->
                        LineLocation(fileLocation, lineId)
                    }
                }
            }
        }
    }

    override fun checkExists(searchCandidate: LineLocation, queryToken: String): Boolean {
        return readLock {
            index[queryToken]
                ?.get(searchCandidate.fileLocation.directoryPath)
                ?.get(searchCandidate.fileLocation.fileName)
                ?.get(searchCandidate.lineIndex) != null
        }
    }

    override fun save(indexDirectory: String) {
        writeLock {
            val indexFile = getIndexFile(indexDirectory)
            if (indexFile.exists()) {
                indexFile.delete()
            }
            indexFile.createNewFile()

            OBJECT_MAPPER.writeValue(indexFile, index)
        }
    }

    override fun load(indexDirectory: String) {
        writeLock {
            val indexFile = getIndexFile(indexDirectory)
            if (!indexFile.exists()) {
                throw IOException("No such file $indexFile")
            }

            index = OBJECT_MAPPER.readValue(indexFile, INDEX_DATA_TYPE_REFERENCE)
        }
    }

    override fun drop(indexDirectory: String) {
        writeLock {
            val indexFile = getIndexFile(indexDirectory)
            if (indexFile.exists()) {
                indexFile.delete()
            }
            index = HashMap()
        }
    }

    private fun getIndexFile(directory: String): File {
        val dir = File(directory)
        if (!dir.exists()) {
            throw IOException("No such directory $directory")
        }
        return dir.resolve("data_index.json")
    }

    private fun <T> writeLock(block: () -> T): T {
        return wLock.use(block)
    }

    private fun <T> readLock(block: () -> T): T {
        return rLock.use(block)
    }

    companion object {

        private val OBJECT_MAPPER = ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)

        private val INDEX_DATA_TYPE_REFERENCE =
            object : TypeReference<TokenIndex>() {}

        private fun <T> Lock.use(block: () -> T): T {
            try {
                lock()
                return block()
            } finally {
                unlock()
            }
        }
    }

}
