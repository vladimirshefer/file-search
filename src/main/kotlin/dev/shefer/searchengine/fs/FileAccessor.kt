package dev.shefer.searchengine.fs

import dev.shefer.searchengine.engine.dto.LineLocation
import java.io.RandomAccessFile
import java.nio.file.Paths

class FileAccessor {

    fun getLine(lineLocation: LineLocation): String {
        val filename = lineLocation.fileLocation.directoryPath + "/" + lineLocation.fileLocation.fileName
        val path = Paths.get(filename)
        val channel = RandomAccessFile(filename, "r")
        for (i in 0 until lineLocation.lineIndex) {
            channel.readLine()
        }
        val line = channel.readLine()
        return line;
    }

}
