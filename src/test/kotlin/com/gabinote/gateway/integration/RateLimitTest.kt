package com.gabinote.gateway.integration

import com.gabinote.gateway.domain.path.Path
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.springframework.cloud.gateway.event.RefreshRoutesEvent

class RateLimitTest : GateWayIntegrationTestTemplate() {
    init {
        feature("Gateway Integration Tests - RateLimit Test") {
            val noPrefixItem = noPrefixItem()
            scenario("제한이 걸리지 않으면 정상 응답이 반환된다") {
                // 1. path 설정
                val targetPath = Path(
                    id = 1L,
                    path = "/no-limit/**",
                    enableAuth = false,
                    priority = 1,
                    _httpMethod = "GET",
                    item = noPrefixItem,
                    replenishRate = 2,
                    burstCapacity = 2,
                )
                insertPath(targetPath)
                applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                // 2. stub 서버 설정
                configureFor(noPrefixItem.port)
                stubFor(
                    get(urlEqualTo("/no-limit/get"))
                        .willReturn(
                            aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""{"name": "no-prefix-api"}""")
                        )
                )

                // 1. 첫번째 요청 - 정상
                Given {
                    port(port)
                }.When {
                    get("/no-limit/get")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("no-prefix-api"))
                }

                // 2. 두번째 요청 - 정상
                Given {
                    port(port)
                }.When {
                    get("/no-limit/get")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("no-prefix-api"))
                }
            }
            scenario("제한이 걸리면 429 응답이 반환된다") {
                // 1. path 설정
                val targetPath = Path(
                    id = 1L,
                    path = "/limited/**",
                    enableAuth = false,
                    priority = 1,
                    _httpMethod = "GET",
                    item = noPrefixItem,
                    replenishRate = 1,
                    burstCapacity = 1,
                )
                insertPath(targetPath)
                applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                // 2. stub 서버 설정
                configureFor(noPrefixItem.port)
                stubFor(
                    get(urlEqualTo("/limited/get"))
                        .willReturn(
                            aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""{"name": "no-prefix-api"}""")
                        )
                )

                // 1. 첫번째 요청 - 정상
                Given {
                    port(port)
                }.When {
                    get("/limited/get")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("no-prefix-api"))
                }

                // 2. 두번째 요청 - 429
                Given {
                    port(port)
                }.When {
                    get("/limited/get")
                }.Then {
                    statusCode(429)

                }
            }
        }
    }
}