import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.6.21"
    // Gradle uses an embedded Kotlin with version 1.4
    // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
    // but it's safe to use 1.6.21, as long as the language level is set to 1.4
    // (the kotlin-dsl plugin does this).
}

// set the versions of Gradle plugins that the subprojects will use here
val kotlinPluginVersion: String = "1.7.10"

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
