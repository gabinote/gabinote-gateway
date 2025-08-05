package com.gabinote.gateway.route.filter

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import java.util.*

@Component
class RequestIdCreateFilterFactory : GatewayFilterFactory<RequestIdCreateFilterFactory.Config> {

    data class Config(
        var header: String = "X-Request-Id"
    )

    override fun newConfig(): Config {
        return Config()
    }

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val request = exchange.request
            val existing = request.headers.getFirst(config.header)
            val requestId = existing ?: UUID.randomUUID().toString()

            val mutatedExchange = exchange.mutate()
                .request(
                    request.mutate()
                        .header(config.header, requestId)
                        .build()
                )
                .build()

            chain.filter(mutatedExchange)
        }
    }
}