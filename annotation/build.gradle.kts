import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

publishing {
    publications {
        create("maven", MavenPublication::class.java) {
            from(components["java"])
            artifactId = "sekret-annotation"
        }
    }
}

bintray {
    user = project.findProperty("bintrayUser") as? String ?: System.getenv("BINTRAY_USER")
    key = project.findProperty("bintrayApiKey") as? String ?: System.getenv("BINTRAY_API_KEY")

    setPublications("maven")

    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "sekret-annotation"
        vcsUrl = "https://github.com/aafanasev/sekret.git"
        publicDownloadNumbers = true

        setLabels("kotlin", "data class", "toString")
        setLicenses("Apache-2.0")
    })
}