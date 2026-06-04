package com.zerodev.clen.data.system

import java.io.File
import javax.inject.Inject

class FileTreeOperations @Inject constructor() {
    fun sizeBytes(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()

        return file.listFiles()
            ?.sumOf { child -> sizeBytes(child) }
            ?: 0L
    }

    fun deleteContents(directory: File): Long {
        if (!directory.exists() || !directory.isDirectory) return 0L

        return directory.listFiles()
            ?.sumOf { child ->
                val sizeBeforeDelete = sizeBytes(child)
                if (child.deleteRecursively()) sizeBeforeDelete else 0L
            }
            ?: 0L
    }
}
