plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.20"
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
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}