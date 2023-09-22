package dev.shefer.searchengine.plugin.file_inspections

import dev.shefer.searchengine.plugin.file_inspections.InspectionFixResult.InspectionFixStatus.*
import dev.shefer.test_internal.TestFilesUtil.assertFilesEquals
import dev.shefer.test_internal.TestFilesUtil.placeTestFile
import dev.shefer.test_internal.TestFilesUtil.testFile
import dev.shefer.test_internal.TestFilesUtil.withTempDirectory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class WrongFileExtensionFileInspectionTest {

    @Test
    fun testRun__Fired__Jpg_Foo() {
        withTempDirectory { testDir ->
            val sourceFileName = "wrongExtension.foo"
            placeTestFile(testDir, "4K.webp.toJpg.jpg.optimized.jpg", sourceFileName)
            val inspectionResult = WrongFileExtensionFileInspection().run(testDir.resolve(sourceFileName))
            Assertions.assertNotNull(inspectionResult)
            Assertions.assertEquals("Wrong extension. Expected jpg, Actual: foo", inspectionResult?.name)
        }
    }

    @Test
    fun testRun__Valid__Jpg() {
        withTempDirectory { testDir ->
            val sourceFileName = "wrongExtension.jpg"
            placeTestFile(testDir, "4K.webp.toJpg.jpg.optimized.jpg", sourceFileName)
            val inspectionResult = WrongFileExtensionFileInspection().run(testDir.resolve(sourceFileName))
            Assertions.assertNull(inspectionResult)
        }
    }

//    @Test
    fun testFix__Failed__Jpg_FileAlreadyExists() {
        withTempDirectory { testDir ->
            val sourceFileName = "wrongExtension.foo"
            val targetFileName = "wrongExtension.jpg"
            val sourceFile = placeTestFile(testDir, "4K.webp.toJpg.jpg.optimized.jpg", sourceFileName)
            val targetFile = placeTestFile(testDir, "4K.webp.toJpg.jpg.optimized.jpg", targetFileName)
            val inspectionFixResult = WrongFileExtensionFileInspection().tryFix(testDir.resolve(sourceFileName))
            Assertions.assertNotNull(inspectionFixResult)
            Assertions.assertEquals(FAILED, inspectionFixResult.status)
            Assertions.assertEquals("Target file already exists", inspectionFixResult.description)
            assertFilesEquals(sourceFile, testFile("4K.webp.toJpg.jpg.optimized.jpg"))
            assertFilesEquals(targetFile, testFile("4K.webp.toJpg.jpg.optimized.jpg"))
        }
    }


    @Test
    fun testFix__Fixed__Jpg() {
        withTempDirectory { testDir ->
            val sourceFileName = "wrongExtension.foo"
            val targetFileName = "wrongExtension.jpg"
            val sourceFile = placeTestFile(testDir, "4K.webp.toJpg.jpg.optimized.jpg", sourceFileName)
            val targetFile = testDir.resolve(targetFileName)
            val inspectionFixResult = WrongFileExtensionFileInspection().tryFix(testDir.resolve(sourceFileName))
            Assertions.assertNotNull(inspectionFixResult)
            Assertions.assertEquals(FIXED, inspectionFixResult.status)
            Assertions.assertEquals("Renamed", inspectionFixResult.description)
            Assertions.assertFalse(sourceFile.exists())
            assertFilesEquals(targetFile, testFile("4K.webp.toJpg.jpg.optimized.jpg"))
        }
    }

    @Test
    fun testFix__NotRequired__UnknownType() {
        withTempDirectory { testDir ->
            val sourceFileName = "wrongExtension.foo"
            val targetFile = testDir.resolve(sourceFileName)
            val fileContent = byteArrayOf(5, 44, 116, 0, 115)
            targetFile.createFile().writeBytes(fileContent)
            val inspectionFixResult = WrongFileExtensionFileInspection().tryFix(testDir.resolve(sourceFileName))
            Assertions.assertNotNull(inspectionFixResult)
            Assertions.assertEquals(NOT_REQUIRED, inspectionFixResult.status)
            Assertions.assertArrayEquals(targetFile.readBytes(), fileContent)
        }
    }

    @Test
    fun testFix__NotRequired__CorrectExtension() {
        withTempDirectory { testDir ->
            val sourceFileName = "wrongExtension.jpg"
            val sourceFile = placeTestFile(testDir, "4K.webp.toJpg.jpg.optimized.jpg", sourceFileName)
            val inspectionFixResult = WrongFileExtensionFileInspection().tryFix(testDir.resolve(sourceFileName))
            Assertions.assertNotNull(inspectionFixResult)
            Assertions.assertEquals(NOT_REQUIRED, inspectionFixResult.status)
            assertFilesEquals(sourceFile, testFile("4K.webp.toJpg.jpg.optimized.jpg"))
        }
    }
}
