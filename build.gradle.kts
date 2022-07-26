plugins {
    base
}

group = "net.afanasev"
version = "0.1.3"

tasks.wrapper {
    gradleVersion = "7.5"
    distributionType = Wrapper.DistributionType.ALL
}
