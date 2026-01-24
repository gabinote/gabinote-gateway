package com.gabinote.gateway.route.filter

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * JWT 클레임을 헤더로 릴레이하는 필터 팩토리
 */
@Component
class ClaimRelayFilterFactory : GatewayFilterFactory<ClaimRelayFilterFactory.Config> {

    data class Config(
        var subHeader: String = "X-Token-Sub",
        var roleHeader: String = "X-Token-Role",
    )

    override fun newConfig(): Config {
        return Config()
    }

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val isInserted: Mono<Boolean> = ReactiveSecurityContextHolder.getContext()
                .flatMap { ctx ->
                    (ctx.authentication.principal as? Jwt)?.let { jwt ->
                        val sub = jwt.subject
                        exchange.request.headers.set(config.subHeader, sub)
                    }
                    val roles = ctx.authentication.authorities.map { it.authority }
                    exchange.request.headers[config.roleHeader] = roles.joinToString(",")

                    chain.filter(exchange).thenReturn(true)
                }
                .defaultIfEmpty(false)

            return@GatewayFilter isInserted.flatMap { handled ->
                if (handled) Mono.empty()
                else chain.filter(exchange)
            }
        }
    }

}