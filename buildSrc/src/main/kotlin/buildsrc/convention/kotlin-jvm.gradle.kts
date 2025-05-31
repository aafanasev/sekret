package buildsrc.convention

plugins {
    id("buildsrc.convention.subproject")
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}
