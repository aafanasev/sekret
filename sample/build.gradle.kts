import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    application
}

dependencies {
    implementation(projects.annotation)
    implementation(projects.kotlinPlugin)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.8")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.8")
    testRuntimeOnly(kotlin("reflect"))
}

application {
    mainClass.set("net.afanasev.sekret.sample.AppKt")
}

val kotlinPlugin = ":kotlin-plugin"

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs =
            listOf("-Xplugin=${project(kotlinPlugin).buildDir}/libs/kotlin-plugin-$version.jar")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeEngines("spek2")
    }
}
