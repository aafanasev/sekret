import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    buildsrc.convention.subproject
    kotlin("multiplatform")
}

description = "Multiplatform usage example, covering the Kotlin/Native (iOS) backend"

kotlin {
    jvmToolchain(17)

    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// Mirrors :sample — the example consumes the compiler plugin straight from the build output
// instead of the published Gradle plugin, so it does not depend on a publish step.
val kotlinPluginJar = project(":kotlin-plugin").layout.buildDirectory
    .file("libs/kotlin-plugin-$version.jar")

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(":kotlin-plugin:jar")

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xplugin=${kotlinPluginJar.get().asFile.path}",
            "-P", "plugin:sekret:annotations=net.afanasev.sekret.sample.kmp.Secret",
            "-P", "plugin:sekret:annotations=net.afanasev.sekret.sample.kmp.Masked",
        )
    }
}
