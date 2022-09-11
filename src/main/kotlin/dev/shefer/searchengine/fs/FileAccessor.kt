package dev.shefer.searchengine.fs

import dev.shefer.searchengine.engine.dto.LineLocation
import java.io.RandomAccessFile

class FileAccessor {

    fun getLine(lineLocation: LineLocation): String {
        val filename = lineLocation.fileLocation.directoryPath + "/" + lineLocation.fileLocation.fileName
        val channel = RandomAccessFile(filename, "r")
        for (i in 0 until lineLocation.lineIndex) {
            channel.readLine()
        }
        val line = channel.readLine()
            ?: throw IllegalStateException("Could not read line from file. Has the file been changed or deleted?")
        return line;
    }

}
