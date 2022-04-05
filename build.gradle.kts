@file:Suppress("PropertyName")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktor_version: String by project
val kotlin_version: String by project
val kotlin_coroutines_version: String by project
val utils_version: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

group = "dev.zieger.file_listing"
version = "0.0.5"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_coroutines_version")

    val kotlinSerializationVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    val ktor_css_version: String by project
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:$ktor_css_version")

    implementation("org.apache.tika:tika-core:2.3.0")

    implementation("dev.zieger.utils:time:$utils_version")
    implementation("dev.zieger.utils:misc:$utils_version")
    implementation("dev.zieger.utils:coroutines:$utils_version")
    implementation("dev.zieger.utils:log:$utils_version")

    implementation("org.slf4j:slf4j-log4j12:1.7.36")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
sourceSets["main"].resources.srcDirs("resources")

tasks {
    named<ShadowJar>("shadowJar") {
        archiveFileName.set("FileListing.jar")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "dev.zieger.file_listing.ApplicationKt"))
        }
        destinationDirectory.set(File(rootProject.projectDir.path))
    }
}
