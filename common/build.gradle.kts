plugins {
    kotlin("jvm")
}

group = "cz.savic.weatherevaluator"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}