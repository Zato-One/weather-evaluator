plugins {
    kotlin("jvm") version "2.1.20" apply false
}

group = "cz.savic.weatherevaluator"
version = "1.0-SNAPSHOT"

val ktorVersion by extra("3.1.2")
val hopliteVersion by extra("2.9.0")

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }
}
