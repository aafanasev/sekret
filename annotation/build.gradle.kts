plugins {
    kotlin("jvm") version "1.3.21"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
