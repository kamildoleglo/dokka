package org.jetbrains.dokka.tests.sanity

import junit.framework.TestCase
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.jetbrains.dokka.MainKt
import org.jetbrains.dokka.tests.common.copy
import org.jetbrains.dokka.tests.common.deleteDirectory
import org.jetbrains.dokka.tests.common.verifyDirsAreEqual
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.nio.file.Path
import java.nio.file.Paths


abstract class AbstractSanityTest {
    @get:Rule val testTmpDir = TemporaryFolder()
    val testDataFolder: Path = Paths.get("src", "test", "resources")
    protected val tmpRootPath: Path by lazy { testTmpDir.root.toPath() }

    fun compareWithReferenceDocumentation(format: String, testDataModuleName: String) {

        val referenceDocsPath = tmpRootPath.resolve("build/reference/$format")
        val docsOutputDir = "dokka"
        val docsOutputPath = tmpRootPath.resolve("build/$docsOutputDir")

        val cliConfig = configureCli(testDataModuleName, docsOutputDir, format)
        MainKt.main(cliConfig)

        verifyDirsAreEqual(docsOutputPath, referenceDocsPath)
    }

    fun compareWithOlderVersion(outputFormat: String, dokkaVersion: String, gradleVersion: String, kotlinVersion: String,
                                testDataModuleName: String) {

        val oldDocsOutputDir = "dokka_old"
        val docsOutputDir = "dokka"

        val result = configureGradle(dokkaVersion, gradleVersion, kotlinVersion, docsOutputDir, outputFormat,
                arguments = arrayOf("dokka", "--stacktrace")).build()
        println(result.output)
        TestCase.assertEquals(TaskOutcome.SUCCESS, result.task(":dokka")?.outcome)

        val docsOutputPath = tmpRootPath.resolve("build/$docsOutputDir")
        val oldDocsOutputPath = tmpRootPath.resolve("build/$oldDocsOutputDir")

        /* You may ask: Why are we generating this in one directory and then copy it to another?
         *  Well we do that, because we don't want errors from different absolute paths in documentation
         *  and this is the simplest fix
         */
        docsOutputPath.copy(oldDocsOutputPath)
        deleteDirectory(docsOutputPath.toFile())

        val cliConfig = configureCli(testDataModuleName, docsOutputDir, outputFormat)
        MainKt.main(cliConfig)

        verifyDirsAreEqual(docsOutputPath, oldDocsOutputPath)
    }

    protected open fun configureGradle(dokkaVersion: String, gradleVersion: String = "4.5", kotlinVersion: String = "1.3.21",
                                  outputDirectory: String = "dokka", outputFormat: String = "html", arguments: Array<String>): GradleRunner {
        return GradleRunner.create()
                .withProjectDir(testTmpDir.root)
                .withArguments("-Ptest_kotlin_version=$kotlinVersion",
                        "-Pdokka_version=$dokkaVersion",
                        "-Poutput_dir=$outputDirectory",
                        "-Poutput_format=$outputFormat",
                        *arguments)
                .withGradleVersion(gradleVersion)
                .withDebug(true)
    }


    protected open fun configureCli(moduleName: String, outputDirectory: String = "dokka", outputFormat: String ="html"): Array<String> {
        return arrayOf(tmpRootPath.toAbsolutePath().toString(),
                "-output", tmpRootPath.toAbsolutePath().toString()+"/build/$outputDirectory",
                "-module", moduleName,
                "-format", outputFormat,
                "-classpath", tmpRootPath.toAbsolutePath().toString() + "/$moduleName/subB/src/main/java")
        // TODO: Fix this, preferably remove and use Gradle plugin as soon as when we remove the fatjar

    }
}