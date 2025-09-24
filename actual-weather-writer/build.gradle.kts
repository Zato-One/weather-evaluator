plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cz.savic.weatherevaluator"

val kotlinXVersion: String by rootProject.extra
val oshaiLoggingVersion: String by rootProject.extra
val julToSlf4jVersion: String by rootProject.extra
val logbackVersion: String by rootProject.extra
val hopliteVersion: String by rootProject.extra
val kafkaVersion: String by rootProject.extra
val oracleDriverVersion: String by rootProject.extra
val liquibaseVersion: String by rootProject.extra
val mybatisVersion: String by rootProject.extra

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinXVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXVersion")

    implementation("io.github.oshai:kotlin-logging-jvm:$oshaiLoggingVersion")
    implementation("org.slf4j:jul-to-slf4j:$julToSlf4jVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:$hopliteVersion")

    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")

    implementation("com.oracle.database.jdbc:ojdbc11:$oracleDriverVersion")
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")
    implementation("org.mybatis:mybatis:$mybatisVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("actual-weather-writer.jar")
    manifest {
        attributes["Main-Class"] = "cz.savic.weatherevaluator.actualweatherwriter.MainKt"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}