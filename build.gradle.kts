plugins {
    base
}

group = "net.afanasev"
version = "0.1.8-SNAPSHOT"

tasks.wrapper {
    gradleVersion = "8.14.1"
    distributionType = Wrapper.DistributionType.ALL
}
