package buildsrc.convention

import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

plugins {
    `maven-publish`
    signing
}

description = "Common configuration for publishing Maven artifacts"

publishing {
    val ossrhUsername = project.findProperty("sonatypeUsername") as? String ?: System.getenv("SONATYPE_USERNAME")
    val ossrhPassword = project.findProperty("sonatypePassword") as? String ?: System.getenv("SONATYPE_PASSWORD")

    repositories {
        maven("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/") {
            name = "ossrh-staging-api"
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

tasks.register("publishConfirm") {
    group = "publishing"
    description = "Makes a POST request to confirm publication"

    doLast {
        val ossrhUsername = project.findProperty("sonatypeUsername") as? String ?: System.getenv("SONATYPE_USERNAME")
        val ossrhPassword = project.findProperty("sonatypePassword") as? String ?: System.getenv("SONATYPE_PASSWORD")

        val url = URL("https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/net.afanasev")
        val credentials = "$ossrhUsername:$ossrhPassword"
        val encodedAuth = Base64.getEncoder().encodeToString(credentials.toByteArray(Charsets.UTF_8))
        val authHeader = "Basic $encodedAuth"

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            setRequestProperty("Authorization", authHeader)
            setRequestProperty("Content-Type", "application/json")
            doOutput = true

            val responseCode = responseCode
            println("POST Response Code: $responseCode")

            val response = inputStream.bufferedReader().use { it.readText() }
            println("Response: $response")
        }
    }
}
