package dev.shefer.searchengine.engine.repository

import dev.shefer.searchengine.FileLocation
import dev.shefer.searchengine.LineLocation
import dev.shefer.searchengine.engine.entity.TokenIndex

class InMemoryTokenRepository : TokenRepository {
    private val INDX: TokenIndex = HashMap()

    override fun registerToken(
        token: String,
        directoryPath: String,
        filename: String,
        lineNumber: Int,
        linePosition: Int
    ) {
        INDX
            .getOrPut(token) { HashMap() }
            .getOrPut(directoryPath) { HashMap() }
            .getOrPut(filename) { HashMap() }
            .getOrPut(lineNumber) { ArrayList() }
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


}
