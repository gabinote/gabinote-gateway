package com.gabinote.gateway.route.filter

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory
import org.springframework.stereotype.Component

@Component
class GatewayAuthenticationCodeFilter : GatewayFilterFactory<GatewayAuthenticationCodeFilter.Config> {

    data class Config(
        var secret: String = "not-set",
        var header: String = "X-Gateway-Secret",
    )

    override fun newConfig(): Config {
        return Config()
    }

    override fun apply(config: Config): GatewayFilter = GatewayFilter { exchange, chain ->
        val request = exchange.request

        val mutatedExchange = exchange.mutate()
            .request(
                request.mutate()
                    .header(config.header, config.secret)
                    .build()
            )
            .build()

        chain.filter(mutatedExchange)
    }
}