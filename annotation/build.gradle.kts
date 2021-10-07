@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
    `maven-publish`
    id("signing")
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    val ossrhUsername = project.findProperty("ossrhUsername") as? String ?: System.getenv("OSSRH_USERNAME")
    val ossrhPassword = project.findProperty("ossrhPassword") as? String ?: System.getenv("OSSRH_PASSWORD")

    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "sekret-annotation"

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            pom {
                name.set("Sekret annotation")
                description.set("Annotations for Sekret library")
                url.set("https://github.com/aafanasev/sekret")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("aafanasev")
                        name.set("Anatolii Afanasev")
                        email.set("tafanasyev@gmail.com")
                    }
                }

                scm {
                    url.set("https://github.com/aafanasev/sekret")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
