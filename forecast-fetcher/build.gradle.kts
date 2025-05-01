plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cz.savic.weatherevaluator"

val kotlinXVersion: String by rootProject.extra
val ktorVersion: String by rootProject.extra
val oshaiLoggingVersion: String by rootProject.extra
val logbackVersion: String by rootProject.extra
val hopliteVersion: String by rootProject.extra
val kafkaVersion: String by rootProject.extra

repositories {
    mavenCentral()
}

dependencies {
    // Project dependencies
    implementation(project(":common"))

    // kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinXVersion")

    // Ktor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:$oshaiLoggingVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")

    // Configuration
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:$hopliteVersion")

    // Kafka
    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")

    // Test
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("forecast-fetcher.jar")
    manifest {
        attributes["Main-Class"] = "cz.savic.weatherevaluator.forecastfetcher.MainKt"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}