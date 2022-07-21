package buildsrc.convention

plugins {
    id("buildsrc.convention.subproject")
    kotlin("jvm")
}

kotlin {
}

java {
    withSourcesJar()
    withJavadocJar()
}
