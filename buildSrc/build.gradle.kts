plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.4.20"
}

// set the versions of Gradle plugins that the subprojects will use here
val kotlinPluginVersion: String = "2.4.20"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinPluginVersion"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinPluginVersion")
}

kotlin {
    jvmToolchain(17)
}
