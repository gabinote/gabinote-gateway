package com.gabinote.gateway.config

import com.gabinote.gateway.route.CustomRouteLocator
import com.gabinote.gateway.route.filter.AuthenticationFilterFactory
import com.gabinote.gateway.route.filter.ClaimRelayFilterFactory
import com.gabinote.gateway.route.filter.CleanRequestFilterFactory
import com.gabinote.gateway.route.filter.RequestIdCreateFilterFactory
import com.gabinote.gateway.service.PathService
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
    ): RouteLocator {
        return CustomRouteLocator(
            pathService,
            routeLocatorBuilder,
            authenticationFilterFactory,
            cleanRequestFilterFactory,
            requestIdCreateFilterFactory,
            claimsRelayFilterFactory
        )
    }
}