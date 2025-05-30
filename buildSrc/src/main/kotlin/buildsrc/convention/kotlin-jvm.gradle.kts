package buildsrc.convention

plugins {
    id("buildsrc.convention.subproject")
    kotlin("jvm")
}

kotlin {
}

java {
    targetCompatibility = JavaVersion.VERSION_18
    sourceCompatibility = JavaVersion.VERSION_18
    withSourcesJar()
    withJavadocJar()
}
