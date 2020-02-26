import org.jetbrains.configureBintrayPublication

plugins {
    id("com.jfrog.bintray")
}

publishing {
    publications {
        register<MavenPublication>("basePlugin") {
            artifactId = "dokka-base"
            from(components["java"])
        }
    }
}

configureBintrayPublication("basePlugin")


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.10")
}
