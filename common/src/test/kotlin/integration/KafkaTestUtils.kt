package cz.savic.weatherevaluator.common.integration

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration

object KafkaTestUtils {

    /**
     * Creates Kafka container with correct advertised listeners for local testing
     */
    fun createCorrectKafkaContainer(): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withEnv("KAFKA_ENABLE_KRAFT", "yes")
            .withEnv("KAFKA_KRAFT_CLUSTER_ID", "test-cluster")
            .withEnv("KAFKA_CFG_NODE_ID", "1")
            .withEnv("KAFKA_CFG_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_CFG_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:19093")
            .withEnv("KAFKA_CFG_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092") // Correct for local
            .withEnv("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", "1@localhost:19093")
            .withEnv("ALLOW_PLAINTEXT_LISTENER", "yes")
            .withEnv("KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withExposedPorts(9092)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
    }

    /**
     * Creates Kafka container with problematic advertised listeners (simulates Docker Compose issue)
     * This reproduces the exact problem where client connects to localhost but server advertises "kafka:9092"
     */
    fun createProblematicKafkaContainer(): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withEnv("KAFKA_ENABLE_KRAFT", "yes")
            .withEnv("KAFKA_KRAFT_CLUSTER_ID", "test-cluster")
            .withEnv("KAFKA_CFG_NODE_ID", "1")
            .withEnv("KAFKA_CFG_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_CFG_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:19093")
            .withEnv("KAFKA_CFG_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092") // This causes the problem!
            .withEnv("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", "1@kafka:19093")
            .withEnv("ALLOW_PLAINTEXT_LISTENER", "yes")
            .withEnv("KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withExposedPorts(9092)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
    }

    /**
     * Creates Kafka container with completely unreachable advertised listeners
     */
    fun createUnreachableKafkaContainer(): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withEnv("KAFKA_ENABLE_KRAFT", "yes")
            .withEnv("KAFKA_KRAFT_CLUSTER_ID", "test-cluster")
            .withEnv("KAFKA_CFG_NODE_ID", "1")
            .withEnv("KAFKA_CFG_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_CFG_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:19093")
            .withEnv("KAFKA_CFG_ADVERTISED_LISTENERS", "PLAINTEXT://nonexistent-host:9092")
            .withEnv("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", "1@localhost:19093")
            .withEnv("ALLOW_PLAINTEXT_LISTENER", "yes")
            .withEnv("KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withExposedPorts(9092)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
    }

    /**
     * Gets bootstrap servers address for a container
     */
    fun getBootstrapServers(container: GenericContainer<*>): String {
        return "localhost:${container.getMappedPort(9092)}"
    }

    /**
     * Waits for connection attempts to fail (useful for testing error scenarios)
     */
    fun waitForConnectionErrors(timeoutMs: Long = 5000) {
        Thread.sleep(timeoutMs)
    }
}