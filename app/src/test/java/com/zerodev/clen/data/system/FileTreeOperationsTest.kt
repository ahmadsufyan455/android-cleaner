package com.zerodev.clen.data.system

import java.io.File
import java.nio.file.Files
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FileTreeOperationsTest {
    private lateinit var root: File
    private val fileTreeOperations = FileTreeOperations()

    @Before
    fun setUp() {
        root = Files.createTempDirectory("clen-cache-test").toFile()
    }

    @After
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun sizeBytesCountsNestedFiles() {
        File(root, "one.tmp").writeText("1234")
        val nested = File(root, "nested").also { it.mkdirs() }
        File(nested, "two.tmp").writeText("123456")

        assertEquals(10L, fileTreeOperations.sizeBytes(root))
    }

    @Test
    fun deleteContentsDeletesChildrenButKeepsRootDirectory() {
        File(root, "one.tmp").writeText("1234")
        val nested = File(root, "nested").also { it.mkdirs() }
        File(nested, "two.tmp").writeText("123456")

        val deletedBytes = fileTreeOperations.deleteContents(root)

        assertEquals(10L, deletedBytes)
        assertTrue(root.exists())
        assertTrue(root.isDirectory)
        assertEquals(0L, fileTreeOperations.sizeBytes(root))
        assertFalse(nested.exists())
    }

    @Test
    fun missingFileHasZeroSize() {
        assertEquals(0L, fileTreeOperations.sizeBytes(File(root, "missing")))
    }
}
