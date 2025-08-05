package com.gabinote.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.cors.reactive.CorsUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Configuration
class PreflightPassConfig {
    @Bean
    fun corsFilter(): WebFilter {
        return WebFilter { ctx: ServerWebExchange, chain: WebFilterChain ->
            val request = ctx.request
            if (CorsUtils.isCorsRequest(request)) {
                val response = ctx.response
                if (request.method === HttpMethod.OPTIONS) {
                    response.statusCode = HttpStatus.OK
                    return@WebFilter Mono.empty()
                }
            }
            chain.filter(ctx)
        }
    }
}