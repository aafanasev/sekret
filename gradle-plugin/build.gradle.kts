plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.11.0"
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}

gradlePlugin {
    plugins {
        create("sekretPlugin") {
            id = "net.afanasev.sekret"
            displayName = "Sekret Gradle plugin"
            description = "Hide sensitive information in toString() of Kotlin Data classes"
            implementationClass = "net.afanasev.sekret.gradle.SekretGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/aafanasev/sekret/"
    vcsUrl = "https://github.com/aafanasev/sekret/"
    tags = listOf("kotlin", "data class", "toString", "secret")

    mavenCoordinates {
        groupId = "net.afanasev"
        artifactId = "sekret-gradle-plugin"
    }
}

