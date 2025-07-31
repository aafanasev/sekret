plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.0"
    // Gradle uses an embedded Kotlin with version 1.4
    // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
}

// set the versions of Gradle plugins that the subprojects will use here
val kotlinPluginVersion: String = "2.2.0"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinPluginVersion"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinPluginVersion")
}

kotlin {
    jvmToolchain(17)
}
