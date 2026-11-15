package buildsrc.convention

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("buildsrc.convention.subproject")
    kotlin("jvm")
}

// Target Java 17 bytecode and API. Java and Kotlin targets must match (KGP checks this), and
// targetCompatibility also sets `org.gradle.jvm.version` in the published module metadata.
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-Xjdk-release=17")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}
