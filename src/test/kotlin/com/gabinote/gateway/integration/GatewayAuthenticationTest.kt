package com.gabinote.gateway.integration

import com.gabinote.gateway.config.properties.GatewaySecretProperties
import com.gabinote.gateway.domain.path.Path
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gateway.event.RefreshRoutesEvent

class GatewayAuthenticationTest : GateWayIntegrationTestTemplate() {

    @Autowired
    lateinit var gatewaySecretProperties: GatewaySecretProperties

    init {
        feature("Gateway Integration Tests - Gateway Authentication Test") {
            feature("prefix 없는 경우") {
                val noPrefixItem = noPrefixItem()

                scenario("게이트웨이 시크릿 헤더가 정상적으로 포함되어야 한다") {
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

                    // 2. stub 서버 설정 - X-Gateway-Secret 헤더 값을 응답으로 반환
                    configureFor(noPrefixItem.port)
                    stubFor(
                        get(urlEqualTo("/test"))
                            .willReturn(
                                aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withTransformers("response-template")
                                    .withBody("""{"gatewaySecret": "{{request.headers.X-Gateway-Secret}}"}""")
                            )
                    )

                    // 3. 게이트웨이 호출 및 검증 - 게이트웨이 시크릿 헤더가 포함되어야 함
                    Given {
                        port(port)
                    }.When {
                        get("/test")
                    }.Then {
                        statusCode(200)
                        body("gatewaySecret", equalTo(gatewaySecretProperties.secretKey))
                    }
                }
            }

            feature("prefix 있는 경우") {
                val prefixItem = prefixItem()

                scenario("게이트웨이 시크릿 헤더가 정상적으로 포함되어야 한다") {
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

                    // 2. stub 서버 설정 - X-Gateway-Secret 헤더 값을 응답으로 반환
                    configureFor(prefixItem.port)
                    stubFor(
                        get(urlEqualTo("/test"))
                            .willReturn(
                                aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withTransformers("response-template")
                                    .withBody("""{"gatewaySecret": "{{request.headers.X-Gateway-Secret}}"}""")
                            )
                    )

                    // 3. 게이트웨이 호출 및 검증 - 게이트웨이 시크릿 헤더가 포함되어야 함
                    Given {
                        port(port)
                    }.When {
                        get("/prefix/test")
                    }.Then {
                        statusCode(200)
                        body("gatewaySecret", equalTo(gatewaySecretProperties.secretKey))
                    }
                }
            }
        }
    }
}