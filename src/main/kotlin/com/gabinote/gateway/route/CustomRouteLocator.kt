package com.gabinote.gateway.route

import com.gabinote.gateway.config.properties.CorsProperties
import com.gabinote.gateway.config.properties.GatewaySecretProperties
import com.gabinote.gateway.config.properties.HeaderProperties
import com.gabinote.gateway.dto.path.service.PathSimpleResServiceDto
import com.gabinote.gateway.route.filter.*
import com.gabinote.gateway.service.PathService
import com.gabinote.gateway.util.rateLimit.RateLimiterFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.BooleanSpec
import org.springframework.cloud.gateway.route.builder.Buildable
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import reactor.core.publisher.Flux
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * DB를 통해 동적으로 라우팅 설정을 제공하는 RouteLocator 구현체
 */
class CustomRouteLocator(
    private val pathService: PathService,
    private val routeLocatorBuilder: RouteLocatorBuilder,
    private val authenticationFilterFactory: AuthenticationFilterFactory,
    private val cleanRequestFilterFactory: CleanRequestFilterFactory,
    private val requestIdCreateFilterFactory: RequestIdCreateFilterFactory,
    private val claimsRelayFilterFactory: ClaimRelayFilterFactory,
    private val gatewayAuthenticationCodeFilter: GatewayAuthenticationCodeFilter,
    private val headerProperties: HeaderProperties,
    private val gatewaySecretProperties: GatewaySecretProperties,
    private val ipKeyResolver: KeyResolver,
    private val rateLimiterFactory: RateLimiterFactory,
    private val globalRequestLoggingFilter: GlobalRequestLoggingFilter,
    private val corsFilterFactory: CorsFilterFactory,
    private val corsProperties: CorsProperties,
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
        logger.info { "[RouteLocator] ${path.item.name} | ${path.httpMethod} | ${if (path.item.prefix == null) "${path.path}" else "/${path.item.prefix}${path.path}"} ${path.role}" }
        val booleanSpec = predicateSpec
            .path(if (path.item.prefix == null) "${path.path}" else "/${path.item.prefix}${path.path}")
            .and()
            .method(path.httpMethod)

        setRewritePrefix(booleanSpec, path.item.prefix)
        setCommonFilters(booleanSpec)


        if (path.enableAuth) {
            logger.debug { "[RouteLocator] Authentication enabled for path: ${path.path} with role: ${path.role}" }
            setAuthenticationFilter(booleanSpec, path)
        }

        if (path.isRateLimitingEnabled()) {
            logger.debug { "[RouteLocator] Rate limiting enabled for path: ${path.path} with replenishRate: ${path.replenishRate}, burstCapacity: ${path.burstCapacity}" }
            setRateLimit(booleanSpec, path)
        }


        return booleanSpec.uri(path.itemUrl())
    }

    private fun setRateLimit(
        booleanSpec: BooleanSpec,
        path: PathSimpleResServiceDto,
    ) {
        booleanSpec.filters { filterSpec ->
            filterSpec.requestRateLimiter {
                it.keyResolver = ipKeyResolver
                it.rateLimiter = rateLimiterFactory.create(
                    path.replenishRate!!,
                    path.burstCapacity!!
                )
            }
        }
    }

    /**
     * 인증 필터 설정 (ROLE 활성화시 호출)
     * @throws IllegalArgumentException role이 null인 경우 예외 발생
     */
    private fun setAuthenticationFilter(
        booleanSpec: BooleanSpec,
        path: PathSimpleResServiceDto,
    ) {
        booleanSpec.filters { filterSpec ->
            filterSpec.filter(authenticationFilterFactory.apply { config ->
                config.role =
                    path.role ?: throw IllegalArgumentException("Role must be provided when enableAuth is true")
            })
        }
    }

    /**
     * prefix가 존재하는 경우, prefix 제거 필터 설정
     */
    private fun setRewritePrefix(route: BooleanSpec, prefix: String?) {
        if (prefix == null) return

        route.filters { filterSpec ->
            filterSpec.stripPrefix(1)
        }
    }

    /**
     * 공통 필터 설정
     * - 요청 정리 필터
     * - 요청 ID 생성 필터
     * - 클레임 릴레이 필터
     */
    private fun setCommonFilters(route: BooleanSpec) {
        route
            .filters { filterSpec ->
                filterSpec
                    .filters(
                        corsFilterFactory.apply { config ->
                            config.allowedOrigins = corsProperties.allowedOrigins
                            config.defaultAllowedOrigin = corsProperties.defaultAllowedOrigin
                        },
                        cleanRequestFilterFactory.apply { config ->
                            config.cleanHeaders = listOf(
                                headerProperties.subHeader,
                                headerProperties.rolesHeader,
                                headerProperties.secretHeader
                            )

                        },
                        requestIdCreateFilterFactory.apply { config ->
                            config.header = headerProperties.requestIdHeader
                        },
                        globalRequestLoggingFilter.apply { config ->
                            config.header = headerProperties.requestIdHeader
                        },
                        claimsRelayFilterFactory.apply { config ->
                            config.subHeader = headerProperties.subHeader
                            config.roleHeader = headerProperties.rolesHeader
                        },
                        gatewayAuthenticationCodeFilter.apply { config ->
                            config.secret = gatewaySecretProperties.secretKey
                            config.header = headerProperties.secretHeader
                        }

                    )
            }
    }

    private fun PathSimpleResServiceDto.isRateLimitingEnabled(): Boolean {
        return this.replenishRate != null && this.burstCapacity != null
    }

}