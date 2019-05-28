import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

application {
    mainClassName = "sekret.sample.AppKt"
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xplugin=${project(":kotlin-plugin").buildDir}/libs/kotlin-plugin.jar")
    }
}
