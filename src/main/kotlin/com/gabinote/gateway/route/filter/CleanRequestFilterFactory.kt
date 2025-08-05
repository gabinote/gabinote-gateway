package com.gabinote.gateway.route.filter

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange

@Component
class CleanRequestFilterFactory : GatewayFilterFactory<CleanRequestFilterFactory.Config> {

    data class Config(
        var cleanHeaders: List<String> = listOf("X-Token-Sub", "X-Token-Roles")
    )

    override fun newConfig(): Config {
        return Config()
    }

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val modifiedRequest = exchange.request.mutate()
                .headers { headers ->
                    config.cleanHeaders.forEach { headers.remove(it) }
                }
                .build()
            chain.filter(exchange.mutate().request(modifiedRequest).build())
        }
    }
}