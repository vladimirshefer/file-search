package dev.shefer.searchengine

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shefer.searchengine.engine.analysis.Analyzer
import dev.shefer.searchengine.engine.dto.FileLocation
import dev.shefer.searchengine.engine.dto.IndexSettings
import dev.shefer.searchengine.engine.dto.LineLocation
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Files.createDirectory
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readLines

abstract class E2eBaseTest {

    private val LOCAL_TEST = true
    private val OVERRIDE_TEST_DATA = false

    val sourceDir = getTestDataDirectory() + "/source"
    val expectedDir = getTestDataDirectory() + "/expected"
    val dataDir = getTestDataDirectory() + "/dist"

    @AfterEach
    fun afterEachBase() {
        deleteDirectory(Paths.get(dataDir).toFile())
    }

    @BeforeEach
    fun beforeEachBase() {
        val dataPath = Paths.get(dataDir)
        if (!dataPath.exists()) {
            createDirectory(dataPath)
        }
    }

    abstract fun analyzer(): Analyzer

    protected val searchEngine: SearchEngine by lazy {
        SearchEngine(
            IndexSettings(
                sourceDir,
                if (OVERRIDE_TEST_DATA) expectedDir else dataDir,
                analyzer()
            )
        )
    }

    private fun deleteDirectory(directoryToBeDeleted: File): Boolean {
        val allContents: Array<File>? = directoryToBeDeleted.listFiles()
        if (allContents != null) {
            for (file in allContents) {
                deleteDirectory(file)
            }
        }
        return directoryToBeDeleted.delete()
    }

    private fun getTestDataDirectory(): String {
        val testDir = "test_data/" + javaClass.simpleName
        if (LOCAL_TEST) {
            val resourceDirectory = Paths.get("src", "test", "resources")
            val resourcePath = resourceDirectory.toFile().getAbsolutePath()
            return "$resourcePath/$testDir"
        }

        val resourceFile = E2eBaseTest::class.java.classLoader
            .getResource(testDir)

        return resourceFile?.path
            ?: fail("resource not found for test")
    }

    protected fun verify() {
        verifyDirsAreEqual(Paths.get(dataDir), Paths.get(expectedDir))
    }

    private fun verifyDirsAreEqual(one: Path, other: Path) {
        Files.walkFileTree(one, object : SimpleFileVisitor<Path?>() {
            override fun visitFile(
                file: Path?,
                attrs: BasicFileAttributes?
            ): FileVisitResult {
                val result: FileVisitResult = super.visitFile(file, attrs)
                // get the relative file name from path "one"
                val relativize: Path = one.relativize(file)
                // construct the path for the counterpart file in "other"
                val fileInOther: Path = other.resolve(relativize)
                verifyFilesEqual(fileInOther, file!!)
                return result
            }
        })
    }

    private fun verifyFilesEqual(fileInOther: Path, file: Path) {
        if (file.name.endsWith(".json")) {
            assertJsonFilesEqual(fileInOther, file)
            return
        }
        val otherBytes = Files.readAllBytes(fileInOther)
        val theseBytes = Files.readAllBytes(file)
        if (!Arrays.equals(otherBytes, theseBytes)) {
            throw AssertionFailedError(file.toString() + " is not equal to " + fileInOther)
        }
    }

    private fun assertJsonFilesEqual(fileInOther: Path, file: Path) {
        val readTree1 = ObjectMapper().readTree(fileInOther.toFile())
        val readTree2 = ObjectMapper().readTree(file.toFile())
        if (!readTree1
                .equals(readTree2)
        ) {
            fail(file.toString() + " json is not equal to " + fileInOther)
        }
    }

    protected fun stupidSearch(queryString: String): List<LineLocation> {
        val searchResults = ArrayList<LineLocation>()
        Files.walkFileTree(
            Paths.get(sourceDir),
            object : SimpleFileVisitor<Path?>() {
                override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                    val result = super.visitFile(file, attrs)
                    if (file == null) {
                        return result
                    }

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
                    return result
                }
            }
        )
        return searchResults
    }

    protected fun verifySearch(queryString: String) {
        val beforeSearch = System.currentTimeMillis()
        val actual = searchEngine.searchService.search(queryString)
        val searchTimeMillis = System.currentTimeMillis() - beforeSearch
        val beforeStupidSearch = System.currentTimeMillis()
        val expected = stupidSearch(queryString)
        val stupidSearchTimeMillis = System.currentTimeMillis() - beforeStupidSearch
        println(this.javaClass.simpleName +
                ": Search `" + queryString + "` is " +
                stupidSearchTimeMillis.toDouble() / searchTimeMillis +
                " times faster (" + stupidSearchTimeMillis + "ms/" + searchTimeMillis + "ms)"
        )
        Assertions.assertEquals(expected, actual)
    }
}
