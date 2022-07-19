package buildsrc.convention

plugins {
    `maven-publish`
    signing
}

description = "Common configuration for publishing Maven artifacts"

publishing {
    val ossrhUsername = project.findProperty("ossrhUsername") as? String ?: System.getenv("OSSRH_USERNAME")
    val ossrhPassword = project.findProperty("ossrhPassword") as? String ?: System.getenv("OSSRH_PASSWORD")

    repositories {
        maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
            name = "sonatype"
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
        // publish to local dir, for testing
        maven(rootProject.layout.buildDirectory.dir("maven-internal")) {
            name = "ProjectLocalDir"
        }
    }
}
