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
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}