package dev.shefer.searchengine.engine.index

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import dev.shefer.searchengine.engine.dto.FileLocation
import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.dto.TokenLocation
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.notExists

private typealias LineIndex = MutableMap<Int, MutableSet<Int>>
private typealias FileIndex = MutableMap<String, LineIndex>
private typealias DirectoryIndex = MutableMap<String, FileIndex>
private typealias TokenIndex = MutableMap<String, DirectoryIndex>

/**
 * Inverted index for text tokens.
 * See https://en.wikipedia.org/wiki/Inverted_index.
 */
class InvertedIndexImpl(
    private val dataPath: Path,
) : InvertedIndex {

    private val dataFile = dataPath.resolve(INDEX_FILENAME)

    private val rwLock = ReentrantReadWriteLock()
    private val rLock = rwLock.readLock()
    private val wLock = rwLock.writeLock()

    private var index: TokenIndex = HashMap()

    override fun registerToken(token: Token) {
        writeLock {
            index
                .getOrPut(token.token) { HashMap() }
                .getOrPut(token.tokenLocation.lineLocation.fileLocation.directoryPath) { HashMap() }
                .getOrPut(token.tokenLocation.lineLocation.fileLocation.fileName) { HashMap() }
                .getOrPut(token.tokenLocation.lineLocation.lineIndex) { HashSet() }
                .add(
                    token.tokenLocation.tokenIndex
                )
        }
    }

    override fun findTokenLocations(token: String): List<TokenLocation> {
        return readLock {
            val get = index[token] ?: emptyMap()
            get.flatMap { (dirName, files) ->
                files.flatMap { (filename, lines) ->
                    val fileLocation = FileLocation(Path.of(dirName, filename))
                    lines.flatMap { (lineId, positions) ->
                        val lineLocation = LineLocation(fileLocation, lineId)
                        positions.map { tokenPosition -> TokenLocation(lineLocation, tokenPosition) }
                    }
                }
            }
        }
    }

    override fun checkExists(token: Token): Boolean {
        return readLock {
            index[token.token]
                ?.get(token.tokenLocation.lineLocation.fileLocation.directoryPath)
                ?.get(token.tokenLocation.lineLocation.fileLocation.fileName)
                ?.get(token.tokenLocation.lineLocation.lineIndex)
                ?.contains(token.tokenLocation.tokenIndex) ?: false
        }
    }

    override fun save() {
        writeLock {
            dataFile.deleteIfExists()
            dataFile.createFile()
            OBJECT_MAPPER.writeValue(dataFile.toFile(), index)
        }
    }

    override fun load() {
        writeLock {
            if (dataFile.notExists()) {
                throw IOException("No such file $dataFile")
            }
            index = OBJECT_MAPPER.readValue(dataFile.toFile(), INDEX_DATA_TYPE_REFERENCE)
        }
    }

    override fun drop() {
        writeLock {
            dataFile.deleteIfExists()
            index = HashMap()
        }
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

        private const val INDEX_FILENAME = "data_index.json"

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
