import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":annotation"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.8")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.8")
    testRuntimeOnly(kotlin("reflect"))
}

application {
    mainClassName = "dev.afanasev.sekret.sample.AppKt"
}

val kotlinPlugin = ":kotlin-plugin"

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xplugin=${project(kotlinPlugin).buildDir}/libs/kotlin-plugin-$version.jar")
    }
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("spek2")
    }
}

tasks {
    "run" {
        dependsOn(project(kotlinPlugin).getTasksByName("build", false))
    }
    "test" {
        dependsOn(project(kotlinPlugin).getTasksByName("build", false))
    }
}
