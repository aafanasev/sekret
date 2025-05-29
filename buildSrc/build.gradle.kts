import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.9.25"
    // Gradle uses an embedded Kotlin with version 1.4
    // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
}

// set the versions of Gradle plugins that the subprojects will use here
val kotlinPluginVersion: String = "1.9.25"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinPluginVersion"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinPluginVersion")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

kotlin {
    kotlinDslPluginOptions {
        jvmTarget.set("17")
    }
}
