plugins {
    kotlin("jvm") version "1.3.21"
    kotlin("kapt") version "1.3.21"
    `java-gradle-plugin`
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("gradle-plugin-api"))

    compileOnly("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
}

gradlePlugin {
    plugins {
        create("sekretPlugin") {
            id = "dev.afanasev.sekret"
            implementationClass = "dev.afanasev.sekret.gradle.SekretGradlePlugin"
        }
    }
}