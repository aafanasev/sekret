package buildsrc.convention

plugins {
    id("buildsrc.convention.subproject")
    kotlin("jvm")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of("8"))
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}
