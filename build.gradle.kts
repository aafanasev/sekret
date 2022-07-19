import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {

    group = "net.afanasev"
    version = "0.1.2"
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.wrapper {
    gradleVersion = "7.5"
    distributionType = Wrapper.DistributionType.ALL
}
