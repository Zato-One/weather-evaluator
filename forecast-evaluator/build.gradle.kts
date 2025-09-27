plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cz.savic.weatherevaluator"

val kotlinXVersion: String by rootProject.extra
val oshaiLoggingVersion: String by rootProject.extra
val logbackVersion: String by rootProject.extra
val hopliteVersion: String by rootProject.extra
val oracleDriverVersion: String by rootProject.extra

repositories {
    mavenCentral()
}

dependencies {
    // Project dependencies
    implementation(project(":common"))

    // kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinXVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXVersion")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:$oshaiLoggingVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")

    // Configuration
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:$hopliteVersion")

    // Database
    implementation("com.oracle.database.jdbc:ojdbc11:$oracleDriverVersion")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinXVersion")
    testImplementation("com.h2database:h2:2.2.224")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("forecast-evaluator.jar")
    manifest {
        attributes["Main-Class"] = "cz.savic.weatherevaluator.forecastevaluator.MainKt"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}