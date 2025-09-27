plugins {
    kotlin("jvm") version "2.1.20" apply false
}

group = "cz.savic.weatherevaluator"
version = "1.0-SNAPSHOT"

val kotlinXVersion by extra("1.8.1")
val ktorVersion by extra("3.1.2")
val oshaiLoggingVersion by extra("7.0.6")
val julToSlf4jVersion by extra("2.1.0-alpha1")
val logbackVersion by extra("1.5.18")
val hopliteVersion by extra("2.9.0")
val kafkaVersion by extra("4.0.0")
val oracleDriverVersion by extra("23.7.0.25.01")
val liquibaseVersion by extra("4.31.1")
val mybatisVersion by extra("3.5.16")

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }
}

tasks.register("startAll") {
    group = "docker"
    description = "Run whole stack (Kafka + all microservices)"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "weather-stack", "up")
        }
    }
}

tasks.register("buildAndStart") {
    group = "docker"
    description = "Build and run whole stack (Kafka + all microservices)"
    dependsOn("startAll")

    doLast {
        exec {
            commandLine("docker-compose", "-p", "weather-stack", "up", "--build")
        }
    }
}

tasks.register("startInfra") {
    group = "docker"
    description = "Start infrastructure (Kafka + Oracle) without services"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "weather-stack", "up", "-d", "kafka", "oracle")
        }
    }
}

tasks.register("stopServices") {
    group = "docker"
    description = "Stop application services (keep infrastructure running)"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "weather-stack", "stop", "forecast-fetcher", "forecast-writer", "actual-weather-fetcher", "actual-weather-writer", "forecast-evaluator")
        }
    }
}

tasks.register("stopInfra") {
    group = "docker"
    description = "Stop infrastructure (Kafka + Oracle) but preserve data"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "weather-stack", "stop", "kafka", "oracle")
        }
    }
}

tasks.register("stopAll") {
    group = "docker"
    description = "Stop all services and infrastructure (preserve data)"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "weather-stack", "stop")
        }
    }
}

tasks.register("resetAll") {
    group = "docker"
    description = "Stop and remove whole stack including data volumes"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "weather-stack", "down", "-v")
        }
    }
}

tasks.register("startKafka") {
    group = "docker"
    description = "Run only Kafka broker by docker-compose.kafka.yml in detached mode"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "kafka-stack", "-f", "docker-compose.kafka.yml", "up", "-d", "--remove-orphans")
        }
    }
}

tasks.register("stopKafka") {
    group = "docker"
    description = "Stop and remove Kafka container"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "kafka-stack", "-f", "docker-compose.kafka.yml", "down")
        }
    }
}

tasks.register("viewKafkaLogs") {
    group = "docker"
    description = "Tail logs from Kafka container"

    doLast {
        exec {
            commandLine("docker", "logs", "-f", "kafka")
        }
    }
}

tasks.register("listKafkaTopics") {
    group = "docker"
    description = "List all Kafka topics from the running Kafka container"

    doLast {
        exec {
            commandLine("docker", "exec", "kafka", "kafka-topics.sh", "--bootstrap-server", "localhost:19092", "--list")
        }
    }
}

tasks.register("startOracle") {
    group = "docker"
    description = "Run only Oracle DB by docker-compose.oracle.yml in detached mode"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "oracle-stack", "-f", "docker-compose.oracle.yml", "up", "-d", "--remove-orphans")
        }
    }
}

tasks.register("stopOracle") {
    group = "docker"
    description = "Stop and remove Oracle DB container"

    doLast {
        exec {
            commandLine("docker-compose", "-p", "oracle-stack", "-f", "docker-compose.oracle.yml", "down")
        }
    }
}

tasks.register("viewOracleLogs") {
    group = "docker"
    description = "Tail logs from Oracle DB container"

    doLast {
        exec {
            commandLine("docker", "logs", "-f", "oracle")
        }
    }
}

tasks.register("testOracleUser") {
    group = "test"
    description = "Runs a full test of Oracle user creation, permissions, and login"

    doLast {
        exec {
            commandLine("bash", "test/scripts/test_oracle_user.sh")
        }
    }
}