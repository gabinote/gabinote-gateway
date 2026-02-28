package com.gabinote.gateway.route.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component


private val logger = KotlinLogging.logger {}

@Component
class CorsFilterFactory : GatewayFilterFactory<CorsFilterFactory.Config> {

    data class Config(
        var allowedOrigins: Set<String> = setOf(),
        var defaultAllowedOrigin: String = "localhost",
    )

    override fun newConfig(): Config {
        return Config()
    }

    override fun apply(config: Config): GatewayFilter = GatewayFilter { exchange, chain ->
        val request = exchange.request
        val response = exchange.response

        // CORS preflight 요청 처리
        if (request.method == HttpMethod.OPTIONS) {
            response.statusCode = HttpStatus.OK
            return@GatewayFilter response.setComplete()
        }
        val requestOrigin = exchange.request.headers.origin

        response.headers.remove("access-control-allow-credentials")
        response.headers.remove("access-control-allow-origin")
        response.headers.remove("Access-Control-Request-Method")

        val allowedOrigin = when {
            requestOrigin == null -> config.defaultAllowedOrigin
            config.allowedOrigins.contains(requestOrigin) -> config.defaultAllowedOrigin
            else -> config.defaultAllowedOrigin
        }

        response.headers.accessControlAllowOrigin = allowedOrigin
        response.headers.accessControlAllowCredentials = true
        response.headers.accessControlAllowMethods = listOf(
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.DELETE,
            HttpMethod.PUT,
            HttpMethod.PATCH,
            HttpMethod.OPTIONS
        )
        response.headers.accessControlAllowHeaders = listOf(
            "x-requested-with",
            "authorization",
            "Content-Type",
            "Content-Length",
            "Authorization",
            "credential",
            "X-XSRF-TOKEN",
            "set-cookie",
            "access-control-expose-headers"
        )

        return@GatewayFilter chain.filter(exchange)
    }
}