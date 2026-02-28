package com.gabinote.gateway.config

import com.gabinote.gateway.config.properties.CorsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
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
@EnableConfigurationProperties(CorsProperties::class)
class CorsConfig(
    private val corsProperties: CorsProperties,
) {


    // 이미 Gateway에서 CORS 설정을 하고 있지만, Preflight 요청은 모든 필터를 무시함. 따라서 해당 필터를 추가하여 Preflight 요청에 대한 CORS 설정을 추가함.
    @Bean
    fun corsFilter(): WebFilter {
        return WebFilter { ctx: ServerWebExchange, chain: WebFilterChain ->
            val request = ctx.request
            if (CorsUtils.isCorsRequest(request)) {
                val response = ctx.response
                val headers = response.headers
                if (request.headers.origin == null) {
                    response.statusCode = HttpStatus.BAD_REQUEST
                    return@WebFilter Mono.empty<Void?>()
                }
                headers.add("Access-Control-Allow-Origin", corsProperties.defaultAllowedOrigin)
                headers.add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS, PATCH")
                headers.add("Access-Control-Allow-Credentials", "true")
                headers.add("Access-Control-Max-Age", "7200")
                headers.add(
                    "Access-Control-Allow-Headers",
                    "x-requested-with, authorization, Content-Type, Content-Length, Authorization, credential, X-XSRF-TOKEN,set-cookie,access-control-expose-headers"
                )
                if (request.method === HttpMethod.OPTIONS) {
                    response.statusCode = HttpStatus.OK
                    return@WebFilter Mono.empty<Void?>()
                }
            }
            chain.filter(ctx)
        }
    }
}