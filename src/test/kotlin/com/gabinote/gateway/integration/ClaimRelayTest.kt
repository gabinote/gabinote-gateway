package com.gabinote.gateway.integration

import com.gabinote.gateway.domain.path.Path
import com.gabinote.gateway.testSupport.keycloak.TestUser
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.springframework.cloud.gateway.event.RefreshRoutesEvent

class ClaimRelayTest : GateWayIntegrationTestTemplate() {
    init {
        beforeTest {
            testKeycloakUtil.recreateRealm()
        }
        feature("Gateway Integration Tests -  Claim Relay Test") {
            val testItem = noPrefixItem()
            beforeTest {
                configureFor(testItem.port)
                stubFor(
                    get(urlEqualTo("/test"))
                        .willReturn(
                            aResponse()
                                .withStatus(200)
                                .withTransformers("response-template")
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                    """
                        {
                            "token_roles": {{#if request.headers.X-Token-Roles}} "{{request.headers.X-Token-Roles}}" {{else}} null {{/if}},
                            "token_sub": {{#if request.headers.X-Token-Sub}} "{{request.headers.X-Token-Sub}}" {{else}} null {{/if}}
                        }
                        """.trimIndent()
                                )
                        )
                )
            }

            feature("인증 옵션이 활성화된 경로에 대해") {

                val enabledPath = Path(
                    id = 1L,
                    path = "/**",
                    enableAuth = true,
                    role = "ROLE_USER",
                    priority = 1,
                    _httpMethod = "GET",
                    item = testItem
                )

                beforeTest {
                    insertPath(enabledPath)
                    applicationEventPublisher.publishEvent(RefreshRoutesEvent(enabledPath))
                }

                scenario("인증 옵션이 활성화 되어있고, 올바른 인증 정보가 있는 경우 해당 유저의 sub와 role이 헤더에 포함되어 전달된다") {
                    val user = TestUser.USER
                    val token = testKeycloakUtil.getTokens(user)
                    Given {
                        port(port)
                        header("Authorization", "Bearer ${token.accessToken}")
                    }.When {
                        get("/test")
                    }.Then {
                        statusCode(200)
                        log().all()
                        body("token_sub", equalTo(user.sub))
                        body("token_roles", containsString("ROLE_USER"))
                    }
                }
            }

            feature("인증 옵션이 비활성화된 경로에 대해") {
                val enabledPath = Path(
                    id = 1L,
                    path = "/**",
                    enableAuth = false,
                    role = null,
                    priority = 1,
                    _httpMethod = "GET",
                    item = testItem
                )

                beforeTest {
                    insertPath(enabledPath)
                    applicationEventPublisher.publishEvent(RefreshRoutesEvent(enabledPath))
                }


                scenario("인증 옵션이 비활성화 되어있고, 올바른 인증 정보가 있는 경우 sub와 role이 헤더에 포함된다.") {
                    val user = TestUser.USER
                    val token = testKeycloakUtil.getTokens(user)
                    Given {
                        port(port)
                        header("Authorization", "Bearer ${token.accessToken}")
                    }.When {
                        get("/test")
                    }.Then {
                        statusCode(200)
                        body("token_sub", equalTo(user.sub))
                        body("token_roles", containsString("ROLE_USER"))
                    }
                }

                scenario("인증 옵션이 비활성화 되어있고, 인증 정보가 없는 경우 sub와 role 헤더는 포함되지 않는다.") {
                    Given {
                        port(port)
                    }.When {
                        get("/test")
                    }.Then {
                        statusCode(200)
                        body("token_sub", equalTo(null))
                        body("token_roles", equalTo(null))
                    }
                }
            }
        }
    }
}