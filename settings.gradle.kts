pluginManagement {
    val kotlin_version: String by settings
    val shadowJarVersion: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlin_version
        id("org.jetbrains.kotlin.plugin.serialization") version kotlin_version
        id("com.github.johnrengelman.shadow") version shadowJarVersion
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
        google()
        maven("https://jitpack.io")

        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://plugins.gradle.org/m2/")
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
        jcenter()
        mavenLocal()

        maven("https://kotlin.bintray.com/ktor")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "file_listing"
