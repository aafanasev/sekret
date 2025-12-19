plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.3.0"
}

// set the versions of Gradle plugins that the subprojects will use here
val kotlinPluginVersion: String = "2.3.0"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinPluginVersion"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinPluginVersion")
}

kotlin {
    jvmToolchain(17)
}
