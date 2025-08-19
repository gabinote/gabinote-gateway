package com.gabinote.gateway.integration

import com.gabinote.gateway.testSupport.db.testDb.DatabaseContainerInitializer
import com.gabinote.gateway.testSupport.keycloak.KeycloakContainerInitializer
import com.gabinote.gateway.testSupport.keycloak.TestKeycloakUtil
import com.gabinote.gateway.testSupport.keycloak.TestUser
import com.gabinote.gateway.testSupport.testApi.TestApiContainerInitializer
import io.kotest.core.spec.style.FeatureSpec
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig.encoderConfig
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.parsing.Parser
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.testcontainers.junit.jupiter.Testcontainers


@Import(TestKeycloakUtil::class)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [DatabaseContainerInitializer::class, KeycloakContainerInitializer::class, TestApiContainerInitializer::class])
class GateWayIntegrationTest : FeatureSpec() {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var databaseClient: DatabaseClient

    @Autowired
    lateinit var testKeycloakUtil: TestKeycloakUtil

    init {
        beforeSpec {
            RestAssured.defaultParser = Parser.JSON
            RestAssured.config = RestAssured.config()
                .encoderConfig(
                    encoderConfig()
                        .defaultContentCharset("UTF-8")
                )
        }

//        afterSpec {
//            println("서버가 실행 중입니다. 강제 종료하려면 Ctrl+C 를 누르세요.")
//            Thread.currentThread().join()
//        }

        feature("Gateway Integration Tests") {
            scenario("아무런 옵션이 없는 기본적인 라우팅 정보가 있을 때, 성공적으로 라우팅된다.") {
                Given {
                    port(port)
                }.When {
                    get("/api1")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("fir-api"))
                }
            }

            scenario("쿼리파라미터와 함께 요청이 주어지면, 해당 쿼리파라미터와 함께 성공적으로 라우팅 된다.") {
                Given {
                    port(port)
                }.When {
                    get("/api1/query?name=123")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("fir-api"))
                    body("message", equalTo(123))
                }
            }

            scenario("패스파라미터와 함께 요청이 주어지면, 해당 패스파라미터와 함께 성공적으로 라우팅 된다.") {
                Given {
                    port(port)
                }.When {
                    get("/api1/path/123")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("fir-api"))
                    body("message", equalTo("123"))
                }
            }

            scenario("올바른 인증 권한을 가진 사용자의 요청이 주어지면, 인증 정보와 함께 성공적으로 라우팅 된다.") {
                Given {
                    port(port)
                    header("Authorization", "Bearer ${testKeycloakUtil.getAccessToken(TestUser.ADMIN)}")
                }.When {
                    get("/api1/need-auth")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("fir-api"))
                    body("sub", equalTo(TestUser.ADMIN.sub))
                    body("role", hasItems("ROLE_ADMIN"))
                }
            }

            scenario("올바르지 않은 인증 권한을 가진 사용자의 요청이 주어지면, 인증 실패 결과를 리턴받는다.") {
                Given {
                    port(port)
                    header("Authorization", "Bearer ${testKeycloakUtil.getAccessToken(TestUser.USER)}")
                }.When {
                    get("/api1/need-auth")
                }.Then {
                    statusCode(401)
                }
            }

            scenario("올바르지 않은 인증 토큰을 가진 사용자의 요청이 주어지면, 인증 실패 결과를 리턴받는다.") {
                Given {
                    port(port)
                    header("Authorization", "Bearer some-wrong-token")
                }.When {
                    get("/api1/need-auth")
                }.Then {
                    statusCode(401)
                }
            }

            scenario("인증이 선택 사항인 경로에 인증 없는 요청이 주어지면, 빈 인증 결과와 함께 라우팅된다.") {
                Given {
                    port(port)
                }.When {
                    get("/api1/optional-auth")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("fir-api"))
                    body("sub", equalTo(null))
                    body("role", equalTo(null))
                }
            }

            scenario("인증이 선택 사항인 경로에 인증 정보가 있는 요청이 주어지면, 인증 결과와 함께 라우팅된다.") {
                Given {
                    port(port)
                    header("Authorization", "Bearer ${testKeycloakUtil.getAccessToken(TestUser.ADMIN)}")
                }.When {
                    get("/api1/optional-auth")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("fir-api"))
                    body("sub", equalTo(TestUser.ADMIN.sub))
                    body("role", hasItems("ROLE_ADMIN"))
                }
            }

            scenario("Path 설정이 비활성화인 Path에 요청하면 404를 리턴한다.") {
                Given {
                    port(port)
                    header("Authorization", "Bearer ${testKeycloakUtil.getAccessToken(TestUser.ADMIN)}")
                }.When {
                    get("/optional-auth")
                }.Then {
                    statusCode(404)
                }
            }

            scenario("path 설정이 되지 않은 경로에 요청하면 실패했다가, 데이터를 추가한 후 reload 한 후 요청을 하면 성공적으로 라우팅된다.") {
                Given {
                    port(port)
                    header("Authorization", "Bearer ${testKeycloakUtil.getAccessToken(TestUser.ADMIN)}")
                }.When {
                    get("/path/123")
                }.Then {
                    statusCode(404)
                }

                databaseClient.sql {
                    """
                        INSERT INTO GATEWAY_PATH (GATEWAY_PATH_PK, GATEWAY_PATH_PATH, GATEWAY_PATH_ENABLE_AUTH, GATEWAY_PATH_ROLE,
                                                  GATEWAY_PATH_HTTP_METHOD, GATEWAY_ITEM_PK, GATEWAY_PATH_PRIORITY, GATEWAY_PATH_IS_ENABLED)
                        VALUES (10, '/path/{id}/**', 0, null, 'GET', 2, 1, 1);
                    """.trimIndent()
                }.then().block()
                Given {
                    port(port)
                    header("Authorization", "Bearer ${testKeycloakUtil.getAccessToken(TestUser.ADMIN)}")
                }.When {
                    post("/management/reload")
                }.Then {
                    statusCode(200)
                }

                Given {
                    port(port)
                    header("Authorization", "Bearer ${testKeycloakUtil.getAccessToken(TestUser.ADMIN)}")
                }.When {
                    get("/path/123")
                }.Then {
                    statusCode(200)
                    body("name", equalTo("sec-api"))
                    body("message", equalTo("123"))
                }

            }

            scenario("관리자가 아닌 사용자가 관리 API에 접근하면 403을 리턴한다.") {
                Given {
                    port(port)
                    header("Authorization", "Bearer ${testKeycloakUtil.getAccessToken(TestUser.USER)}")
                }.When {
                    get("/management/reload")
                }.Then {
                    statusCode(403)
                }
            }
        }
    }

}