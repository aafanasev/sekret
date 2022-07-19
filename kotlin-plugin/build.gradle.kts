@file:Suppress("UnstableApiUsage")

import buildsrc.config.createSekretPom

plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    kotlin("kapt")
    buildsrc.convention.`maven-publish`
}

description = "Kotlin compiler for Sekret library"

dependencies {
    implementation(kotlin("compiler-embeddable"))

    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "sekret-kotlin-plugin"

            from(components["java"])

            createSekretPom {
                name.set("Sekret compiler - Kotlin plugin")
                description.set(project.description)
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
