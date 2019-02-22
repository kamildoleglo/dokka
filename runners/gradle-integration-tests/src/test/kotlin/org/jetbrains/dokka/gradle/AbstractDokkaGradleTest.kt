package org.jetbrains.dokka.gradle


import com.intellij.rt.execution.junit.FileComparisonFailure
import org.gradle.testkit.runner.GradleRunner
import org.junit.ComparisonFailure
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


val testDataFolder = Paths.get("testData")

val pluginClasspathData = Paths.get("build", "createClasspathManifest", "dokka-plugin-classpath.txt")
val androidPluginClasspathData = pluginClasspathData.resolveSibling("android-dokka-plugin-classpath.txt")

val dokkaFatJarPathData = pluginClasspathData.resolveSibling("fatjar.txt")

val androidLocalProperties = testDataFolder.resolve("android.local.properties").let { if (Files.exists(it)) it else null }

abstract class AbstractDokkaGradleTest {
    @get:Rule val testProjectDir = TemporaryFolder()

    open val pluginClasspath: List<File> = pluginClasspathData.toFile().readLines().map { File(it) }

    fun checkOutputStructure(expected: String, actualSubpath: String) {
        val expectedPath = testDataFolder.resolve(expected)
        val actualPath = testProjectDir.root.toPath().resolve(actualSubpath).normalize()

        assertEqualsIgnoringSeparators(expectedPath.toFile(), buildString {
            actualPath.toFile().writeStructure(this, File(actualPath.toFile(), "."))
        })
    }

    fun checkNoErrorClasses(actualSubpath: String, extension: String = "html", errorClassMarker: String = "ERROR CLASS") {
        val actualPath = testProjectDir.root.toPath().resolve(actualSubpath).normalize()
        var checked = 0
        Files.walk(actualPath).filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".$extension") }.forEach {
            val text = it.toFile().readText()

            val noErrorClasses = text.replace(errorClassMarker, "?!")

            if (noErrorClasses != text) {
                throw FileComparisonFailure("", noErrorClasses, text, null)
            }

            checked++
        }
        println("$checked files checked for error classes")
    }

    fun checkNoUnresolvedLinks(actualSubpath: String, extension: String = "html", marker: Regex = "[\"']#[\"']".toRegex()) {
        val actualPath = testProjectDir.root.toPath().resolve(actualSubpath).normalize()
        var checked = 0
        Files.walk(actualPath).filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".$extension") }.forEach {
            val text = it.toFile().readText()

            val noErrorClasses = text.replace(marker, "?!")

            if (noErrorClasses != text) {
                throw FileComparisonFailure("", noErrorClasses, text, null)
            }

            checked++
        }
        println("$checked files checked for unresolved links")
    }

    fun checkExternalLink(actualSubpath: String, link: String, extension: String = "html") {
        val actualPath = testProjectDir.root.toPath().resolve(actualSubpath).normalize()
        var filesChecked = 0
        var totalEntries = 0

        Files.walk(actualPath).filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".$extension") }.forEach {
            val text: String = it.toFile().readText()
            totalEntries += countSubstring(text, link)
            filesChecked++
        }
        println("$filesChecked files checked for valid external links '$link', found $totalEntries links")
        if (totalEntries < 1) {
            throw ComparisonFailure("link not found", ">0", totalEntries.toString())
        }
    }

    private fun countSubstring(s: String, sub: String): Int = s.split(sub).size - 1

    fun configure(gradleVersion: String = "3.5", kotlinVersion: String = "1.1.2", arguments: Array<String>): GradleRunner {
        val fatjar = dokkaFatJarPathData.toFile().readText()

        return GradleRunner.create().withProjectDir(testProjectDir.root)
                .withArguments("-Pdokka_fatjar=$fatjar", "-Ptest_kotlin_version=$kotlinVersion", *arguments)
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(gradleVersion)
                .withDebug(true)
    }
}