package com.gabinote.gateway.testSupport.stubServer

import com.gabinote.image.testSupport.testConfig.container.ContainerNetworkHelper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent

private val logger = KotlinLogging.logger {}

class TestStubServerContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {


    override fun initialize(context: ConfigurableApplicationContext) {
        val firApiPort = ContainerNetworkHelper.getAvailablePort()
        val firApi = WireMockServer(
            WireMockConfiguration.options()
                .port(firApiPort)
                .extensions(ResponseTemplateTransformer(false))
        )
        firApi.start()

        val secApiPort = ContainerNetworkHelper.getAvailablePort()
        val secApi = WireMockServer(
            WireMockConfiguration.options()
                .port(secApiPort)
                .extensions(ResponseTemplateTransformer(false))
        )
        secApi.start()

        logger.debug { "Fir stub server started on ${firApi.port()}" }
        logger.debug { "Sec stub server started on ${secApi.port()}" }
        TestPropertyValues.of(
            "test.api.prefix-port=${firApiPort}",
            "test.api.no-prefix-port=${secApiPort}",
        ).applyTo(context.environment)
        context.addApplicationListener { event ->
            if (event is ContextClosedEvent) {
                firApi.stop()
                secApi.stop()
            }
        }


    }


}