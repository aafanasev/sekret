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

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("net.afanasev.sekret.sample.AppKt")
}

val kotlinPlugin = ":kotlin-plugin"

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs =
            listOf(
                "-Xplugin=${project(kotlinPlugin).layout.buildDirectory.asFile.get().path}/libs/kotlin-plugin-$version.jar",
            )
        pluginOptions.add(CompilerPluginConfig().apply {
            addPluginArgument("sekret", SubpluginOption("annotations","net.afanasev.sekret.Secret"))
            addPluginArgument("sekret", SubpluginOption("annotations","net.afanasev.sekret.sample.AnnotationWithReplacement"))
        })
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}