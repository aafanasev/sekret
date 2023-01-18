import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.7.10"
    // Gradle uses an embedded Kotlin with version 1.4
    // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
}

// set the versions of Gradle plugins that the subprojects will use here
val kotlinPluginVersion: String = "1.8.0"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinPluginVersion"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinPluginVersion")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

kotlin {
    kotlinDslPluginOptions {
        jvmTarget.set("1.8")
    }
}
