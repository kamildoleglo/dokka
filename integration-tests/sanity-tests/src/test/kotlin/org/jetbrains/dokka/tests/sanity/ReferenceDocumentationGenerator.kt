package org.jetbrains.dokka.tests.sanity

import org.jetbrains.dokka.MainKt
import org.jetbrains.dokka.tests.common.deleteDirectory
import java.nio.file.Path
import java.nio.file.Paths

class ReferenceDocumentationGenerator{
    private val testDataFolder: Path = Paths.get("src", "test", "resources")

    fun generateReferenceDocumentation(testDataModuleName: String, format: String) {
        val oldReferenceDocsPath = testDataFolder.resolve(testDataModuleName).resolve("build/reference/$format")
        deleteDirectory(oldReferenceDocsPath.toFile())

        val config = configureCli(testDataModuleName, format, format)
        MainKt.main(config)
    }

    private fun configureCli(moduleName: String, outputDirectory: String, outputFormat: String): Array<String> {
        return arrayOf(testDataFolder.toAbsolutePath().toString(),
                "-output", testDataFolder.resolve(moduleName).toAbsolutePath().toString()+"/build/reference/$outputDirectory",
                "-module", moduleName,
                "-format", outputFormat,
                "-classpath", testDataFolder.resolve(moduleName).toAbsolutePath().toString() + "/subB/src/main/java")
        // TODO: Fix this, preferably remove and use Gradle plugin as soon as when we remove the fatjar
    }

    companion object {

        //Run using Gradle task: regenerateDocumentation
        @JvmStatic
        fun main(args: Array<String>){
            ReferenceDocumentationGenerator().generateReferenceDocumentation(args[0], args[1])
        }
    }
}
