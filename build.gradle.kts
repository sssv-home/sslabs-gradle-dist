import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.BIN

plugins {
    `distribute-gradle`
}

group = "com.sslabs.gradle-dist"
version = "8.6-1.0"

tasks.wrapper {
    gradleVersion = "8.6"
    distributionType = BIN
}

repositories {
    mavenCentral()
}
