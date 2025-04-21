plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cz.savic.weatherevaluator"

val ktorVersion: String by rootProject.extra
val hopliteVersion: String by rootProject.extra

repositories {
    mavenCentral()
}

dependencies {
    // Project dependencies
    implementation(project(":common"))
    
    // Ktor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.6")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")

    // Configuration
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:$hopliteVersion")

    // Kafka
    implementation("org.apache.kafka:kafka-clients:4.0.0")

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