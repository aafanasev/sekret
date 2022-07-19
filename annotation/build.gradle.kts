import buildsrc.config.createSekretPom

plugins {
    buildsrc.convention.subproject
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`maven-publish`
}

description = "Annotations for Sekret library"

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "sekret-annotation"

            from(components["java"])

            createSekretPom {
                name.set("Sekret annotation")
                description.set(project.description)
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
