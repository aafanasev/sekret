plugins {
    kotlin("jvm") version "1.3.21"
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

application {
    mainClassName = "sekret.sample.AppKt"
}
