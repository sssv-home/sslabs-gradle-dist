plugins {
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
}
