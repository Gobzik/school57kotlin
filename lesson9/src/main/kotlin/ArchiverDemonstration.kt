fun main() {

    val archiver = Archiver()

    println("Только лог файлы")
    archiver.createZipArchive(
        sourceDirPath = "lesson9/src/main/kotlin/test_project",
        zipFilePath = "lesson9/src/main/kotlin/logs_only.zip",
        filterExtensions = arrayOf(".log")
    )

    println("Только тхт файлы")
    archiver.createZipArchive(
        sourceDirPath = "lesson9/src/main/kotlin/test_project",
        zipFilePath = "lesson9/src/main/kotlin/text_only.zip",
        filterExtensions = arrayOf(".txt")
    )

    println("Все файлы")
    archiver.createZipArchive(
        sourceDirPath = "lesson9/src/main/kotlin/test_project",
        zipFilePath = "lesson9/src/main/kotlin/all.zip",
        filterExtensions = arrayOf()
    )
}