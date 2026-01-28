package com.gabinote.gateway.testSupport.redis

import com.redis.testcontainers.RedisContainer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.utility.DockerImageName

private val logger = KotlinLogging.logger {}

class RedisContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        val redisContainer = RedisContainer(
            DockerImageName.parse("redis:8.4")
        ).apply {
            withLabel("test-container", "redis")
            withReuse(true)
        }
    }

    override fun initialize(context: ConfigurableApplicationContext) {
        redisContainer.start()
        logger.debug { "Redis started on port ${redisContainer.firstMappedPort}" }
        TestPropertyValues.of(
            "spring.data.redis.host=${redisContainer.host}",
            "spring.data.redis.port=${redisContainer.firstMappedPort}"
        ).applyTo(context.environment)
    }
}