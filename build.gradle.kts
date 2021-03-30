import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31" apply false
}

allprojects {

    group = "dev.afanasev"
    version = "0.0.9"

    repositories {
        jcenter()
    }

}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
