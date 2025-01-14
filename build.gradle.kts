plugins {
    base
}

group = "net.afanasev"
version = "0.1.5"

tasks.wrapper {
    gradleVersion = "7.6"
    distributionType = Wrapper.DistributionType.ALL
}
