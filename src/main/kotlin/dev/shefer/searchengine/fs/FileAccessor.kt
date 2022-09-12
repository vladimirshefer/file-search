package dev.shefer.searchengine.fs

import dev.shefer.searchengine.engine.dto.LineLocation
import java.io.RandomAccessFile

class FileAccessor {

    /**
     * Reads the turns the line of the file.
     * TODO: Optimize reads (with RandomAccessFile?)
     */
    fun getLine(lineLocation: LineLocation): String {
        val filename = lineLocation.fileLocation.directoryPath + "/" + lineLocation.fileLocation.fileName
        val channel = RandomAccessFile(filename, "r")
        for (i in 0 until lineLocation.lineIndex) {
            channel.readLine()
        }
        return channel.readLine()
            ?: throw IllegalStateException("Could not read line from file. Has the file been changed or deleted?")
    }

}
