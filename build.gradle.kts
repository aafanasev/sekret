plugins {
    base
}

group = "net.afanasev"
version = "1.0.0"

tasks.wrapper {
    gradleVersion = "8.14.1"
    distributionType = Wrapper.DistributionType.ALL
}
