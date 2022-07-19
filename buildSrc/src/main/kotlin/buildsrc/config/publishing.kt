package buildsrc.config

import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication

fun MavenPublication.createSekretPom(
    configure: MavenPom.() -> Unit
): Unit = pom {
    // Note: Gradle will automatically set the POM 'group' and 'name' from the subproject group and name
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

    configure()
}
