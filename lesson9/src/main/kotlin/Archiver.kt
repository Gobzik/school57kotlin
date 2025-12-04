import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Archiver {

    fun createZipArchive(
        sourceDirPath: String,
        zipFilePath: String,
        filterExtensions: Array<String> = arrayOf(".txt", ".log")
    ) {
        val sourceDir = File(sourceDirPath)

        if (!sourceDir.exists() || !sourceDir.isDirectory) {
            println("Ошибка: каталог $sourceDirPath не существует или не является каталогом")
            return
        }

        FileOutputStream(zipFilePath).use { fos ->
            ZipOutputStream(fos).use { zos ->
                println("=".repeat(60))
                println("Создание архива: $zipFilePath")
                println("Исходный каталог: ${sourceDir.absolutePath}")
                println("Разрешенные расширения: ${filterExtensions.joinToString(", ")}")
                println("=".repeat(60))

                val fileCount = archiveDirectory(sourceDir, sourceDir, zos, filterExtensions)

                println("=".repeat(60))
                println("✓ Архив успешно создан!")
                println("✓ Добавлено файлов: $fileCount")
                println("=".repeat(60))
            }
        }
    }

    private fun archiveDirectory(
        sourceDir: File,
        currentDir: File,
        zos: ZipOutputStream,
        filterExtensions: Array<String>
    ): Int {
        var fileCount = 0

        currentDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                fileCount += archiveDirectory(sourceDir, file, zos, filterExtensions)
            } else {
                if (matchesExtension(file, filterExtensions)) {
                    val relativePath = getRelativePath(sourceDir, file)

                    if (addFileToZip(file, relativePath, zos)) {
                        fileCount++
                    }
                }
            }
        }

        return fileCount
    }

    private fun matchesExtension(file: File, filterExtensions: Array<String>): Boolean {
        val fileName = file.name.lowercase()

        if (filterExtensions.isEmpty() || filterExtensions.contains("")) {
            return true
        }

        return filterExtensions.any { ext ->
            fileName.endsWith(ext.lowercase())
        }
    }

    private fun getRelativePath(sourceDir: File, file: File): String {
        val sourcePath = sourceDir.absolutePath
        val filePath = file.absolutePath

        return if (filePath.startsWith(sourcePath)) {
            filePath.substring(sourcePath.length + 1)
        } else {
            file.name
        }
    }

    private fun addFileToZip(file: File, entryName: String, zos: ZipOutputStream): Boolean {
        return try {
            FileInputStream(file).use { fis ->
                val zipEntry = ZipEntry(entryName)
                zos.putNextEntry(zipEntry)

                val buffer = ByteArray(1024)
                var length: Int
                var totalBytes = 0L

                while (fis.read(buffer).also { length = it } >= 0) {
                    zos.write(buffer, 0, length)
                    totalBytes += length
                }

                zos.closeEntry()

                val sizeKB = totalBytes / 1024.0
                val indent = " ".repeat(maxOf(0, 50 - entryName.length))
                println("✓ $entryName${indent}│ ${"%.1f".format(sizeKB)} KB")

                true
            }
        } catch (e: Exception) {
            println("✗ Ошибка при обработке файла ${file.path}: ${e.message}")
            false
        }
    }
}