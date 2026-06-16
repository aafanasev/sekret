plugins {
    base
}

group = "net.afanasev"
version = "2.4.0"

tasks.wrapper {
    gradleVersion = "9.0.0"
    distributionType = Wrapper.DistributionType.ALL
}
