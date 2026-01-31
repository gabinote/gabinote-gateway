package com.gabinote.gateway.route.filter

import com.gabinote.gateway.util.exception.AuthenticationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger { }

/**
 * 주어진 역할(role)을 가진 사용자만 요청을 통과시킵니다.
 */
@Component
class AuthenticationFilterFactory : GatewayFilterFactory<AuthenticationFilterFactory.Config> {

    data class Config(
        var role: String,
    )

    override fun newConfig(): Config {
        return Config("DEFAULT")
    }

    override fun apply(config: Config): GatewayFilter = GatewayFilter { exchange, chain ->

        val authCheck: Mono<Boolean> = ReactiveSecurityContextHolder.getContext()
            .map { ctx ->
                val roles = ctx.authentication.authorities.map { it.authority }
                roles.contains(config.role)
            }
            .defaultIfEmpty(false)

        return@GatewayFilter authCheck.flatMap { allowed ->
            if (allowed) {
                chain.filter(exchange)
            } else {
                Mono.error(AuthenticationException("User lacks role: ${config.role}"))
            }
        }
    }
}