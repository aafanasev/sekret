plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.3.1"
}

dependencies {
    // Both are always already on the consumer's buildscript classpath — KGP-api is provided by the
    // Kotlin Gradle plugin (or by AGP 9's built-in Kotlin), the stdlib by Gradle's embedded Kotlin.
    // Exposing them at runtime puts a second Kotlin toolchain on that classpath and breaks
    // resolution of the `org.jetbrains:annotations:{strictly 13.0}` pin. See issue #90.
    compileOnly(kotlin("gradle-plugin-api"))
    compileOnly(kotlin("stdlib"))

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")

    // `compileOnly` above keeps these off the consumer's runtime classpath, so the test
    // classpath has to ask for them explicitly.
    testImplementation(kotlin("gradle-plugin-api"))
    testImplementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks {
    jar {
        manifest {
            attributes("Implementation-Version" to project.version)
        }
    }
}
gradlePlugin {
    website.set("https://github.com/aafanasev/sekret/")
    vcsUrl.set("https://github.com/aafanasev/sekret/")
    plugins {
        create("sekretPlugin") {
            id = "net.afanasev.sekret"
            displayName = "Sekret Gradle plugin"
            description = "Hide sensitive information in toString() of Kotlin Data classes"
            implementationClass = "net.afanasev.sekret.gradle.SekretGradlePlugin"
            tags.set(listOf("kotlin", "data class", "toString", "secret"))
        }
    }
}



