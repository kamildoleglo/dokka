package org.jetbrains.dokka.tests.common

import com.intellij.rt.execution.junit.FileComparisonFailure
import junit.framework.AssertionFailedError
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes


fun File.writeStructure(builder: StringBuilder, relativeTo: File = this, spaces: Int = 0) {
    builder.append(" ".repeat(spaces))
    val out = if (this != relativeTo) this.relativeTo(relativeTo) else this

    builder.append(out)
    if (this.isDirectory) {
        builder.appendln("/")
        this.listFiles().sortedBy { it.name }.forEach { it.writeStructure(builder, this, spaces + 4) }
    } else {
        builder.appendln()
    }
}

fun assertEqualsIgnoringSeparators(expectedFile: File, output: String) {
    if (!expectedFile.exists()) expectedFile.createNewFile()
    val expectedText = expectedFile.readText().replace("\r\n", "\n")
    val actualText = output.replace("\r\n", "\n")

    if (expectedText != actualText)
        throw FileComparisonFailure("", expectedText, actualText, expectedFile.canonicalPath)
}

class CopyFileVisitor(private var sourcePath: Path?, private val targetPath: Path) : SimpleFileVisitor<Path>() {

    @Throws(IOException::class)
    override fun preVisitDirectory(dir: Path,
                                   attrs: BasicFileAttributes): FileVisitResult {
        if (sourcePath == null) {
            sourcePath = dir
        } else {
            Files.createDirectories(targetPath.resolve(sourcePath?.relativize(dir)))
        }
        return FileVisitResult.CONTINUE
    }

    @Throws(IOException::class)
    override fun visitFile(file: Path,
                           attrs: BasicFileAttributes): FileVisitResult {
        Files.copy(file, targetPath.resolve(sourcePath?.relativize(file)), StandardCopyOption.REPLACE_EXISTING)
        return FileVisitResult.CONTINUE
    }
}

fun Path.copy(to: Path) {
    Files.walkFileTree(this, CopyFileVisitor(this, to))
}


fun verifyDirsAreEqual(first: Path, second: Path) {
    var differences = 0

    class CompareFileVisitor : SimpleFileVisitor<Path>() {
        override fun visitFile(filePath: Path, attrs: BasicFileAttributes): FileVisitResult {
            val result = super.visitFile(filePath, attrs)

            val relativize: Path = first.relativize(filePath)
            val fileInOtherPath: Path = second.resolve(relativize)

            println("=== comparing: {$filePath} to {$fileInOtherPath}")

            val thisContent = filePath.toFile().readLines()
                    .map(String::trimIndent).map { it.replace("<!--.*-->".toRegex(),"") }

            val otherContent = fileInOtherPath.toFile().readLines()
                    .map(String::trimIndent).map { it.replace("<!--.*-->".toRegex(),"") }


            val thisIterator = thisContent.iterator()
            val otherIterator = otherContent.iterator()

            while(thisIterator.hasNext() && otherIterator.hasNext()){
                var expected = thisIterator.next()
                while(expected.isBlank() && thisIterator.hasNext()) {
                    expected = thisIterator.next()
                }

                var current = otherIterator.next()
                while(current.isBlank() && otherIterator.hasNext()) {
                    current = otherIterator.next()
                }

                if (expected != current) {
                    differences++
                    println("!!! File comparison failed \r\n Expected '$expected' \r\n Actual: '$current")
                }
            }
            return result
        }
    }
    Files.walkFileTree(first, CompareFileVisitor())
    if (differences > 0){
        throw AssertionFailedError("Found $differences differences")
    }
}

fun deleteDirectory(directoryToBeDeleted: File): Boolean {
    val allContents = directoryToBeDeleted.listFiles()
    if (allContents != null) {
        for (file in allContents) {
            deleteDirectory(file)
        }
    }
    return directoryToBeDeleted.delete()
}