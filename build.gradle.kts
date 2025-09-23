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
    description = "Run whole stack (Kafka + all microservices) in WSL"

    doLast {
        exec {
            commandLine("wsl", "docker-compose", "up", "--build")
        }
    }
}

tasks.register("startKafka") {
    group = "docker"
    description = "Run only Kafka broker by docker-compose.kafka.yml in WSL in detached mode"

    doLast {
        exec {
            commandLine("wsl", "docker-compose", "-p", "kafka-stack", "-f", "docker-compose.kafka.yml", "up", "-d", "--remove-orphans")
        }
    }
}

tasks.register("stopKafka") {
    group = "docker"
    description = "Stop and remove Kafka container"

    doLast {
        exec {
            commandLine("wsl", "docker-compose", "-f", "docker-compose.kafka.yml", "down")
        }
    }
}

tasks.register("viewKafkaLogs") {
    group = "docker"
    description = "Tail logs from Kafka container"

    doLast {
        exec {
            commandLine("wsl", "docker", "logs", "-f", "kafka")
        }
    }
}

tasks.register("listKafkaTopics") {
    group = "docker"
    description = "List all Kafka topics from the running Kafka container"

    doLast {
        exec {
            commandLine("wsl", "docker", "exec", "kafka", "kafka-topics.sh", "--bootstrap-server", "localhost:9092", "--list")
        }
    }
}

tasks.register("startOracle") {
    group = "docker"
    description = "Run only Oracle DB by docker-compose.oracle.yml in WSL in detached mode"

    doLast {
        exec {
            commandLine("wsl", "docker-compose", "-p", "oracle-stack", "-f", "docker-compose.oracle.yml", "up", "-d", "--remove-orphans")
        }
    }
}

tasks.register("stopOracle") {
    group = "docker"
    description = "Stop and remove Oracle DB container"

    doLast {
        exec {
            commandLine("wsl", "docker-compose", "-f", "docker-compose.oracle.yml", "down")
        }
    }
}

tasks.register("viewOracleLogs") {
    group = "docker"
    description = "Tail logs from Oracle DB container"

    doLast {
        exec {
            commandLine("wsl", "docker", "logs", "-f", "oracle")
        }
    }
}

tasks.register("testOracleUser") {
    group = "test"
    description =
        "Runs a full test of Oracle user creation, permissions, and login. Must be run in bash. Before this run cygpath -w /usr/bin/bash to convert path to Windows."

    doLast {
        exec {
            commandLine("C:/Program Files/Git/usr/bin/bash.exe", "test/scripts/test_oracle_user.sh")
        }
    }
}