import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0" apply false
}

allprojects {

    group = "net.afanasev"
    version = "0.1.0-RC1"

    repositories {
        jcenter()
    }

}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
