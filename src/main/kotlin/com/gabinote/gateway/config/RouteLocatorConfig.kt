package com.gabinote.gateway.config

import com.gabinote.gateway.config.properties.CorsProperties
import com.gabinote.gateway.config.properties.GatewaySecretProperties
import com.gabinote.gateway.config.properties.HeaderProperties
import com.gabinote.gateway.route.CustomRouteLocator
import com.gabinote.gateway.route.filter.*
import com.gabinote.gateway.service.PathService
import com.gabinote.gateway.util.rateLimit.RateLimiterFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RouteLocatorConfig {
    @Bean
    fun routeLocator(
        pathService: PathService,
        routeLocatorBuilder: RouteLocatorBuilder,
        authenticationFilterFactory: AuthenticationFilterFactory,
        cleanRequestFilterFactory: CleanRequestFilterFactory,
        requestIdCreateFilterFactory: RequestIdCreateFilterFactory,
        claimsRelayFilterFactory: ClaimRelayFilterFactory,
        gatewayAuthenticationCodeFilter: GatewayAuthenticationCodeFilter,
        headerProperties: HeaderProperties,
        gatewaySecretProperties: GatewaySecretProperties,
        @Qualifier("ipKeyResolver")
        keyResolver: KeyResolver,
        rateLimiterFactory: RateLimiterFactory,
        globalRequestLoggingFilter: GlobalRequestLoggingFilter,
        corsProperties: CorsProperties,
        corsFilterFactory: CorsFilterFactory,

        ): RouteLocator {
        return CustomRouteLocator(
            pathService,
            routeLocatorBuilder,
            authenticationFilterFactory,
            cleanRequestFilterFactory,
            requestIdCreateFilterFactory,
            claimsRelayFilterFactory,
            gatewayAuthenticationCodeFilter,
            headerProperties,
            gatewaySecretProperties,
            keyResolver,
            rateLimiterFactory,
            globalRequestLoggingFilter,
            corsFilterFactory,
            corsProperties
        )
    }
}