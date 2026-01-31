package com.gabinote.gateway.testSupport.stubServer

import com.gabinote.image.testSupport.testConfig.container.ContainerNetworkHelper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

private val logger = KotlinLogging.logger {}

class TestStubServerContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        @JvmStatic
        val firApi: WireMockServer by lazy {
            val port = ContainerNetworkHelper.getAvailablePort()
            WireMockServer(
                WireMockConfiguration.options()
                    .port(port)
                    .extensions(ResponseTemplateTransformer(false))
            ).also {
                it.start()
                logger.debug { "Fir stub server started on ${it.port()}" }
            }
        }

        @JvmStatic
        val secApi: WireMockServer by lazy {
            val port = ContainerNetworkHelper.getAvailablePort()
            WireMockServer(
                WireMockConfiguration.options()
                    .port(port)
                    .extensions(ResponseTemplateTransformer(false))
            ).also {
                it.start()
                logger.debug { "Sec stub server started on ${it.port()}" }
            }
        }
    }

    override fun initialize(context: ConfigurableApplicationContext) {
        val firApiPort = firApi.port()
        val secApiPort = secApi.port()

        TestPropertyValues.of(
            "test.api.prefix-port=$firApiPort",
            "test.api.no-prefix-port=$secApiPort",
        ).applyTo(context.environment)
    }

}