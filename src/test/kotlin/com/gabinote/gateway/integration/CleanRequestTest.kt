package com.gabinote.gateway.integration

import com.gabinote.gateway.domain.path.Path
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.springframework.cloud.gateway.event.RefreshRoutesEvent

class CleanRequestTest : GateWayIntegrationTestTemplate() {
    init {
        feature("Gateway Integration Tests - Clean Request Test") {
            feature("prefix 없는 경우") {
                val noPrefixItem = noPrefixItem()

                scenario("인증 헤더, 게이트웨이 시크릿 헤더 가 포함된 요청이 들어오면, 해당 헤더들이 제거된 상태로 백엔드에 전달된다.") {
                    // 1. path 설정
                    val targetPath = Path(
                        id = 1L,
                        path = "/**",
                        enableAuth = false,
                        priority = 1,
                        _httpMethod = "GET",
                        item = noPrefixItem
                    )
                    insertPath(targetPath)
                    applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                    // 2. stub 서버 설정
                    configureFor(noPrefixItem.port)
                    stubFor(
                        get(urlPathEqualTo("/get"))
                            .willReturn(
                                aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withTransformers("response-template")
                                    .withBody(
                                        """
                    {
                        "token_roles": {{#if request.headers.X-Token-Roles}}true{{else}}false{{/if}},
                        "token_sub": {{#if request.headers.X-Token-Sub}}true{{else}}false{{/if}},
                    }
                """.trimIndent()
                                    )
                            )
                    )

                    // 3. 게이트웨이로 요청 전송
                    Given {
                        port(port)
                        header("X-Token-Roles", "admin,user")
                        header("X-Token-Sub", "user-123")
                    }.When {
                        get("/get")
                    }.Then {
                        statusCode(200)
                        body("token_roles", equalTo(false))
                        body("token_sub", equalTo(false))
                    }

                }
            }

            feature("prefix 있는 경우") {
                val prefixItem = prefixItem()
                scenario("인증 헤더, 게이트웨이 시크릿 헤더 가 포함된 요청이 들어오면, 해당 헤더들이 제거된 상태로 백엔드에 전달된다.") {
                    // 1. path 설정
                    val targetPath = Path(
                        id = 1L,
                        path = "/**",
                        enableAuth = false,
                        priority = 1,
                        _httpMethod = "GET",
                        item = prefixItem
                    )
                    insertPath(targetPath)
                    applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                    // 2. stub 서버 설정
                    configureFor(prefixItem.port)
                    stubFor(
                        get(urlPathEqualTo("/get"))
                            .willReturn(
                                aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withTransformers("response-template")
                                    .withBody(
                                        """
                    {
                        "token_roles": {{#if request.headers.X-Token-Roles}}true{{else}}false{{/if}},
                        "token_sub": {{#if request.headers.X-Token-Sub}}true{{else}}false{{/if}},
                    }
                """.trimIndent()
                                    )
                            )
                    )

                    // 3. 게이트웨이로 요청 전송
                    Given {
                        port(port)
                        header("X-Token-Roles", "admin,user")
                        header("X-Token-Sub", "user-123")
                    }.When {
                        get("/prefix/get")
                    }.Then {
                        statusCode(200)
                        body("token_roles", equalTo(false))
                        body("token_sub", equalTo(false))
                    }

                }
            }
        }
    }
}