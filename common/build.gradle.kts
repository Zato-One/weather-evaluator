plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.20"
}

group = "cz.savic.weatherevaluator"

repositories {
    mavenCentral()
}

dependencies {
    // kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")

    // Integration Tests
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:kafka:1.19.3")
    testImplementation("org.apache.kafka:kafka-clients:4.0.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}