package buildsrc.convention

plugins {
    id("buildsrc.convention.subproject")
    kotlin("jvm")
}

kotlin {
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}
