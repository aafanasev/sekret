import org.jetbrains.kotlin.gradle.plugin.CompilerPluginConfig
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    application
}

dependencies {
    implementation(projects.annotation)
    implementation(projects.kotlinPlugin)

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.spekframework.spek2:spek-dsl:2.0.18")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.18")
    testRuntimeOnly(kotlin("reflect"))
}

application {
    mainClass.set("net.afanasev.sekret.sample.AppKt")
}

val kotlinPlugin = ":kotlin-plugin"

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs =
            listOf(
                "-Xplugin=${project(kotlinPlugin).layout.buildDirectory}/libs/kotlin-plugin-$version.jar",
            )
        pluginOptions.add(CompilerPluginConfig().apply {
            addPluginArgument("sekret", SubpluginOption("annotations","net.afanasev.sekret.sample.AnnotationWithReplacement"))
            addPluginArgument("sekret", SubpluginOption("annotations","net.afanasev.sekret.Secret"))
        })
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeEngines("spek2")
    }
}