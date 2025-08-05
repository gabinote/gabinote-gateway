package com.gabinote.gateway.testSupport.testApi

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration

private val logger = KotlinLogging.logger { }

class TestApiContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        @JvmStatic
        val firApi: GenericContainer<*> = GenericContainer(
            DockerImageName.parse("gabinote/gateway-test-api:latest")
        ).withEnv("UVICORN_PORT", "30001")
            .withEnv("APP_NAME", "fir-api")
            .waitingFor(
                Wait.forHttp("/")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofSeconds(60))
            ).apply {
                portBindings = listOf("30001:30001")
            }


        @JvmStatic
        val secApi: GenericContainer<*> = GenericContainer(
            DockerImageName
                .parse("gabinote/gateway-test-api:latest")
        ).withEnv("UVICORN_PORT", "30002")
            .withEnv("APP_NAME", "sec-api")
            .withExposedPorts(30002)
            .waitingFor(
                Wait.forHttp("/")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofSeconds(60))
            ).apply {
                portBindings = listOf("30002:30002")
            }
    }

    override fun initialize(context: ConfigurableApplicationContext) {
        firApi.start()
        logger.debug { "Fir api started on ${firApi.host} : ${firApi.firstMappedPort}" }
        secApi.start()
        logger.debug { "Sec api started on ${secApi.host} : ${secApi.firstMappedPort}" }
    }
}