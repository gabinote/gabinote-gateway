package com.gabinote.gateway.route.filter

import com.gabinote.gateway.util.log.RequestLoggingHelper
import com.gabinote.gateway.util.log.ResponseLoggingHelper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


private val logger = KotlinLogging.logger {}

@Component
class GlobalRequestLoggingFilter : GatewayFilterFactory<GlobalRequestLoggingFilter.Config> {

    data class Config(
        var header: String = "X-Request-Id",
    )

    override fun newConfig(): Config {
        return Config()
    }

    override fun apply(config: Config): GatewayFilter = GatewayFilter { exchange, chain ->
        val request = exchange.request
        val requestId = request.headers.getFirst(config.header) ?: "N/A"
        logger.info {
            RequestLoggingHelper.build(request, requestId)
        }

        chain.filter(exchange).then(
            Mono.fromRunnable {
                logger.info {
                    ResponseLoggingHelper.build(request, exchange.response.statusCode, requestId)
                }
            }
        )
    }
}