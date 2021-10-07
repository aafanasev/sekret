import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31" apply false
}

allprojects {

    group = "net.afanasev"
    version = "0.1.1-RC3"

    repositories {
        mavenCentral()
    }

}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
