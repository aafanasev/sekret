plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.3.1"
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}

gradlePlugin {
    website.set("https://github.com/aafanasev/sekret/")
    vcsUrl.set("https://github.com/aafanasev/sekret/")
    plugins {
        create("sekretPlugin") {
            id = "net.afanasev.sekret"
            displayName = "Sekret Gradle plugin"
            description = "Hide sensitive information in toString() of Kotlin Data classes"
            implementationClass = "net.afanasev.sekret.gradle.SekretGradlePlugin"
            tags.set(listOf("kotlin", "data class", "toString", "secret"))
        }
    }
}



