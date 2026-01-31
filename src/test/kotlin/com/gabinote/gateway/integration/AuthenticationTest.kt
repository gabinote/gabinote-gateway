package com.gabinote.gateway.integration

import com.gabinote.gateway.domain.path.Path
import com.gabinote.gateway.testSupport.keycloak.TestUser
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.springframework.cloud.gateway.event.RefreshRoutesEvent

class AuthenticationTest : GateWayIntegrationTestTemplate() {


    init {
        beforeTest {
            testKeycloakUtil.recreateRealm()
        }
        feature("Gateway Integration Tests -  Authentication Test") {

            val testItem = noPrefixItem()
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
                configureFor(testItem.port)
                stubFor(
                    get(urlEqualTo("/test"))
                        .willReturn(
                            aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""{"name": "no-prefix-api"}""")
                        )
                )
                insertPath(enabledPath)
                applicationEventPublisher.publishEvent(RefreshRoutesEvent(enabledPath))

            }

            scenario("인증 옵션이 활성화 되어있고, 올바른 인증 정보가 있는 경우 200 응답이 반환된다") {

                val token = testKeycloakUtil.getTokens(TestUser.USER)
                Given {
                    port(port)
                    header("Authorization", "Bearer ${token.accessToken}")
                }.When {
                    get("/test")
                }.Then {
                    statusCode(200)
                }
            }

            scenario("인증 옵션이 활성화 되어있는데, 인증 정보가 없는 경우 403 에러가 발생한다") {

                Given {
                    port(port)
                }.When {
                    get("/test")
                }.Then {
                    statusCode(401)
                }
            }

            scenario("인증 옵션이 활성화 되어있는데, 올바른 인증 정보가 없는 경우 403 에러가 발생한다") {

                Given {
                    port(port)
                    header("Authorization", "Bearer invalid_token")
                }.When {
                    get("/test")
                }.Then {
                    statusCode(401)
                }
            }

            scenario("인증옵션이 활성화 되어있는데, 해당 role이 없는 경우 403 에러가 발생한다") {


                val token = testKeycloakUtil.getTokens(TestUser.ADMIN)
                Given {
                    port(port)
                    header("Authorization", "Bearer ${token.accessToken}")
                }.When {
                    get("/test")
                }.Then {
                    statusCode(401)
                }
            }

        }
    }
}