plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cz.savic.weatherevaluator"

val hopliteVersion: String by rootProject.extra
val mybatisVersion: String by rootProject.extra
val oracleDriverVersion: String by rootProject.extra

repositories {
    mavenCentral()
}

dependencies {
    // Project dependencies
    implementation(project(":common"))
    
    // kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.6")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")

    // Configuration
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:$hopliteVersion")

    // Kafka
    implementation("org.apache.kafka:kafka-clients:4.0.0")
    
    // Database
    implementation("org.mybatis:mybatis:$mybatisVersion")
    implementation("com.oracle.database.jdbc:ojdbc11:$oracleDriverVersion")
    
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
    archiveFileName.set("forecast-writer.jar")
    manifest {
        attributes["Main-Class"] = "cz.savic.weatherevaluator.forecastwriter.MainKt"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}