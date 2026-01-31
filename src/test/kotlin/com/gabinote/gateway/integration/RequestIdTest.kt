package com.gabinote.gateway.integration

import com.gabinote.gateway.domain.path.Path
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.springframework.cloud.gateway.event.RefreshRoutesEvent

class RequestIdTest : GateWayIntegrationTestTemplate() {
    init {
        feature("Gateway Integration Tests - Request Id Test") {
            feature("prefix 없는 경우") {
                val noPrefixItem = noPrefixItem()

                scenario("request id가 없으면 생성되어야 한다") {
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

                    // 2. stub 서버 설정 - X-Request-Id 헤더 값을 응답으로 반환
                    configureFor(noPrefixItem.port)
                    stubFor(
                        get(urlEqualTo("/test"))
                            .willReturn(
                                aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withTransformers("response-template")
                                    .withBody("""{"requestId": "{{request.headers.X-Request-Id}}"}""")
                            )
                    )

                    // 3. 게이트웨이 호출 및 검증 - X-Request-Id 없이 요청
                    Given {
                        port(port)
                    }.When {
                        get("/test")
                    }.Then {
                        statusCode(200)
                        body("requestId", notNullValue())
                    }
                }

                scenario("request id가 있으면 그대로 전달되어야 한다") {
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

                    // 2. stub 서버 설정 - X-Request-Id 헤더 값을 응답으로 반환
                    configureFor(noPrefixItem.port)
                    stubFor(
                        get(urlEqualTo("/test"))
                            .willReturn(
                                aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withTransformers("response-template")
                                    .withBody("""{"requestId": "{{request.headers.X-Request-Id}}"}""")
                            )
                    )

                    val existingRequestId = "my-custom-request-id-12345"

                    // 3. 게이트웨이 호출 및 검증 - 기존 X-Request-Id 헤더와 함께 요청
                    Given {
                        port(port)
                        header("X-Request-Id", existingRequestId)
                    }.When {
                        get("/test")
                    }.Then {
                        statusCode(200)
                        body("requestId", equalTo(existingRequestId))
                    }
                }
            }

            feature("prefix 있는 경우") {
                val prefixItem = prefixItem()

                scenario("request id가 없으면 생성되어야 한다") {
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

                    // 2. stub 서버 설정 - X-Request-Id 헤더 값을 응답으로 반환
                    configureFor(prefixItem.port)
                    stubFor(
                        get(urlEqualTo("/test"))
                            .willReturn(
                                aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withTransformers("response-template")
                                    .withBody("""{"requestId": "{{request.headers.X-Request-Id}}"}""")
                            )
                    )

                    // 3. 게이트웨이 호출 및 검증 - X-Request-Id 없이 요청
                    Given {
                        port(port)
                    }.When {
                        get("/prefix/test")
                    }.Then {
                        statusCode(200)
                        body("requestId", notNullValue())
                    }
                }

                scenario("request id가 있으면 그대로 전달되어야 한다") {
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

                    // 2. stub 서버 설정 - X-Request-Id 헤더 값을 응답으로 반환
                    configureFor(prefixItem.port)
                    stubFor(
                        get(urlEqualTo("/test"))
                            .willReturn(
                                aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withTransformers("response-template")
                                    .withBody("""{"requestId": "{{request.headers.X-Request-Id}}"}""")
                            )
                    )

                    val existingRequestId = "my-custom-request-id-12345"

                    // 3. 게이트웨이 호출 및 검증 - 기존 X-Request-Id 헤더와 함께 요청
                    Given {
                        port(port)
                        header("X-Request-Id", existingRequestId)
                    }.When {
                        get("/prefix/test")
                    }.Then {
                        statusCode(200)
                        body("requestId", equalTo(existingRequestId))
                    }
                }
            }
        }
    }
}