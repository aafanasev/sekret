plugins {
    base
}

group = "net.afanasev"
version = "0.1.7"

tasks.wrapper {
    gradleVersion = "7.6"
    distributionType = Wrapper.DistributionType.ALL
}
