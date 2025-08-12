package com.gabinote.gateway.route

import com.gabinote.gateway.dto.path.service.PathSimpleResServiceDto
import com.gabinote.gateway.route.filter.AuthenticationFilterFactory
import com.gabinote.gateway.route.filter.ClaimRelayFilterFactory
import com.gabinote.gateway.route.filter.CleanRequestFilterFactory
import com.gabinote.gateway.route.filter.RequestIdCreateFilterFactory
import com.gabinote.gateway.service.PathService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.BooleanSpec
import org.springframework.cloud.gateway.route.builder.Buildable
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import reactor.core.publisher.Flux
import java.util.*

private val logger = KotlinLogging.logger {}

class CustomRouteLocator(
    private val pathService: PathService,
    private val routeLocatorBuilder: RouteLocatorBuilder,
    private val authenticationFilterFactory: AuthenticationFilterFactory,
    private val cleanRequestFilterFactory: CleanRequestFilterFactory,
    private val requestIdCreateFilterFactory: RequestIdCreateFilterFactory,
    private val claimsRelayFilterFactory: ClaimRelayFilterFactory,
) : RouteLocator {

    override fun getRoutes(): Flux<Route> {
        logger.info { "[RouteLocator] Start Set Up Routes" }
        val routeBuilder = routeLocatorBuilder.routes()
        return setUpRoutes(routeBuilder)
            .doOnComplete { logger.info { "[RouteLocator] Set Up Routes Completed" } }
            .doOnError { error -> logger.error(error) { "[RouteLocator] Error while setting up routes" } }
    }

    private fun setUpRoutes(routesBuilder: RouteLocatorBuilder.Builder): Flux<Route> {
        return pathService.getAllPath()
            .map { route ->
                routesBuilder.route(UUID.randomUUID().toString())
                { predicateSpec -> setPredicateSpec(route, predicateSpec) }
            }
            .collectList()
            .flatMapMany { routesBuilder.build().routes }
    }

    private fun setPredicateSpec(path: PathSimpleResServiceDto, predicateSpec: PredicateSpec): Buildable<Route> {
        logger.info { "[RouteLocator] ${path.item.name} | ${path.httpMethod} | /${path.item.prefix}${path.path} ${path.role}" }
        val booleanSpec = predicateSpec
            .path("/${path.item.prefix}${path.path}")
            .and()
            .method(path.httpMethod)

        setRewritePrefix(booleanSpec, path.item.prefix)
        setCommonFilters(booleanSpec)

        if (path.enableAuth) {
            booleanSpec.filters { filterSpec ->
                filterSpec.filter(authenticationFilterFactory.apply { config ->
                    config.role =
                        path.role ?: throw IllegalArgumentException("Role must be provided when enableAuth is true")
                })
            }
        }
        return booleanSpec.uri(path.itemUrl())
    }

    private fun setRewritePrefix(route: BooleanSpec, prefix: String?) {
        if (prefix == null) return

        route.filters { filterSpec ->
            filterSpec.stripPrefix(1)
        }
    }

    private fun setCommonFilters(route: BooleanSpec) {
        route
            .filters { filterSpec ->
                filterSpec
                    .filters(
                        cleanRequestFilterFactory.apply { config ->
                            CleanRequestFilterFactory.Config()
                        },
                        requestIdCreateFilterFactory.apply { config ->
                            RequestIdCreateFilterFactory.Config()
                        },
                        claimsRelayFilterFactory.apply { config ->
                            ClaimRelayFilterFactory.Config()
                        }
                    )
            }
    }

}