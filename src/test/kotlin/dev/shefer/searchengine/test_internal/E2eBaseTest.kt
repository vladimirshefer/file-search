package dev.shefer.searchengine.test_internal

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shefer.searchengine.SearchEngine
import dev.shefer.searchengine.engine.config.Analyzer
import dev.shefer.searchengine.engine.config.IndexSettings
import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.test_internal.util.StupidSearchService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import java.io.File
import java.nio.file.Files
import java.nio.file.Files.createDirectory
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.name

abstract class E2eBaseTest {

    companion object {
        private const val OVERRIDE_TEST_DATA = false
    }

    private val sourceDir: String = getTestDataDirectory() + "/source"
    private val expectedDir: String = getTestDataDirectory() + "/expected"
    private val dataDir: String = getTestDataDirectory() + "/dist"

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
        val resourceDirectory = Paths.get("src", "test", "resources")
        val resourcePath = resourceDirectory.toFile().absolutePath
        return "$resourcePath/$testDir"
    }

    protected fun verifyIndexFiles() {
        verifyDirsAreEqual(Paths.get(expectedDir), Paths.get(dataDir))
    }

    private fun verifyDirsAreEqual(one: Path, other: Path) {
        StupidSearchService.forEachFile(other.toString()) { file ->
            val relativize: Path = other.relativize(file)
            val fileInOther: Path = one.resolve(relativize)
            assertTrue(fileInOther.exists()) {
                "file $relativize does not exist in $one, but exists in $other"
            }
        }
        StupidSearchService.forEachFile(one.toString()) { file ->
            val relativize: Path = one.relativize(file)
            val fileInOther: Path = other.resolve(relativize)
            verifyFilesEqual(fileInOther, file)
        }
    }

    private fun verifyFilesEqual(fileInOther: Path, file: Path) {
        if (file.name.endsWith(".json")) {
            assertJsonFilesEqual(fileInOther, file)
            return
        }
        val otherBytes = Files.readAllBytes(fileInOther)
        val theseBytes = Files.readAllBytes(file)
        if (!Arrays.equals(otherBytes, theseBytes)) {
            throw AssertionFailedError("$file is not equal to $fileInOther")
        }
    }

    private fun assertJsonFilesEqual(fileInOther: Path, file: Path) {
        val readTree1 = ObjectMapper().readTree(fileInOther.toFile())
        val readTree2 = ObjectMapper().readTree(file.toFile())
        if (!readTree1.equals(readTree2)) {
            fail("$file json is not equal to $fileInOther")
        }
    }

    private fun stupidSearch(queryString: String): List<LineLocation> {
        return StupidSearchService(sourceDir).search(queryString)
    }

    protected fun verifySearch(queryString: String) {
        val actual: List<LineLocation> = searchEngine.searchService.search(queryString)
        val expected: List<LineLocation> = stupidSearch(queryString)
        Assertions.assertEquals(expected.toSet(), actual.toSet())

        val searchTimeMillis = measureTime { searchEngine.searchService.search(queryString) }
        val stupidSearchTimeMillis = measureTime { stupidSearch(queryString) }
        val speedupRate = stupidSearchTimeMillis / searchTimeMillis

        println(
            "${this.javaClass.simpleName}:" +
                    " Search `$queryString` is $speedupRate times faster" +
                    " (${stupidSearchTimeMillis}ms/${searchTimeMillis}ms)." +
                    " // ${expected.size} search results."
        )
    }

    private fun measureTime(block: () -> Any?): Double {
        val baseline = measureTimeInternal { "" }.second
        val time = measureTimeInternal(block).second
        return time - baseline
    }

    private fun measureTimeInternal(block: () -> Any?): Pair<Any?, Double> {
        var actual: Any? = ""
        val times = 10000
        val beforeSearch = System.currentTimeMillis()
        for (i in 1..times) {
            actual = block()
        }
        val searchTimeMillis = (System.currentTimeMillis() - beforeSearch) / times.toDouble()
        return Pair(actual, searchTimeMillis)
    }

}
