package org.jetbrains.dokka.tests.sanity

import org.jetbrains.dokka.tests.common.copy
import org.junit.Before
import org.junit.Test


class SanityTest: AbstractSanityTest() {
    private val testDataModuleName = "multiProjectSingleOut"

    @Before
    fun prepareTestData() {
        testDataFolder.resolve(testDataModuleName).copy(tmpRootPath)
    }

    @Test
    fun `test dokka 0_9_17 and current html`() {
        compareWithOlderVersion("html", "0.9.17", "4.5", "1.3.21", testDataModuleName)
    }

    @Test
    fun `test dokka 0_9_17 and current javadoc`() {
        compareWithOlderVersion("javadoc", "0.9.17", "4.5", "1.3.21", testDataModuleName)
    }

    @Test
    fun `test dokka 0_9_17 and current markdown`() {
        compareWithOlderVersion("markdown", "0.9.17", "4.5", "1.3.21", testDataModuleName)
    }

    @Test
    fun `test with reference html`(){
        compareWithReferenceDocumentation("html", testDataModuleName)
    }

    @Test
    fun `test with reference javadoc`(){
        compareWithReferenceDocumentation("javadoc", testDataModuleName)
    }

    @Test
    fun `test with reference markdown`(){
        compareWithReferenceDocumentation("markdown", testDataModuleName)
    }

}
