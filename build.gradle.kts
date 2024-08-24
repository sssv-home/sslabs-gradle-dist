import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.BIN

plugins {
    `distribute-gradle`
}

group = "com.sslabs.gradle-dist"
version = "8.10-1.0"

tasks.wrapper {
    gradleVersion = "8.10"
    distributionType = BIN
}

repositories {
    mavenCentral()
}
