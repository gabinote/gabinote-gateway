package com.gabinote.gateway.integration

import com.gabinote.gateway.domain.path.Path
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.springframework.cloud.gateway.event.RefreshRoutesEvent

// 기본 기능 테스트를 위한 베이스 클래스
class BaseTest : GateWayIntegrationTestTemplate() {
    init {
        feature("Gateway Integration Tests - Base Test") {
            feature("기본 라우팅 테스트") {
                feature("prefix 없는 경우") {
                    val noPrefixItem = noPrefixItem()
                    scenario("[GET] /get 엔드포인트 호출 시 200 응답을 반환한다") {
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
                            get(urlEqualTo("/get"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""{"name": "no-prefix-api"}""")
                                )
                        )

                        // 3. 게이트웨이 호출 및 검증
                        Given {
                            port(port)
                        }.When {
                            get("/get")
                        }.Then {
                            statusCode(200)
                            body("name", equalTo("no-prefix-api"))
                        }
                    }

                    // HTTP 메서드들
                    scenario("[POST] /post 엔드포인트 호출 시 200 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "POST",
                            item = noPrefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(noPrefixItem.port)
                        stubFor(
                            post(urlEqualTo("/post"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""{"name": "no-prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                            contentType("application/json")
                        }.When {
                            post("/post")
                        }.Then {
                            statusCode(200)
                            body("name", equalTo("no-prefix-api"))
                        }
                    }

                    scenario("[PUT] /put 엔드포인트 호출 시 200 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "PUT",
                            item = noPrefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(noPrefixItem.port)
                        stubFor(
                            put(urlEqualTo("/put"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""{"name": "no-prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                            contentType("application/json")
                        }.When {
                            put("/put")
                        }.Then {
                            statusCode(200)
                            body("name", equalTo("no-prefix-api"))
                        }
                    }

                    scenario("[DELETE] /delete 엔드포인트 호출 시 200 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "DELETE",
                            item = noPrefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(noPrefixItem.port)
                        stubFor(
                            delete(urlEqualTo("/delete"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""{"name": "no-prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                        }.When {
                            delete("/delete")
                        }.Then {
                            statusCode(200)
                            body("name", equalTo("no-prefix-api"))
                        }
                    }

                    // 쿼리 파라미터
                    scenario("[GET] /query 쿼리 파라미터 없이 호출 시 message가 null인 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/query/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "GET",
                            item = noPrefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(noPrefixItem.port)
                        stubFor(
                            get(urlPathEqualTo("/query"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withTransformers("response-template")
                                        .withBody("""{"message": {{#if request.query.name}}{{request.query.name}}{{else}}null{{/if}}, "name": "no-prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                        }.When {
                            get("/query")
                        }.Then {
                            statusCode(200)
                            body("message", nullValue())
                            body("name", equalTo("no-prefix-api"))
                        }
                    }

                    scenario("[GET] /query?name=123 쿼리 파라미터와 함께 호출 시 해당 값이 포함된 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/query/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "GET",
                            item = noPrefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(noPrefixItem.port)
                        stubFor(
                            get(urlPathEqualTo("/query"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withTransformers("response-template")
                                        .withBody("""{"message": {{#if request.query.name}}{{request.query.name}}{{else}}null{{/if}}, "name": "no-prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                            queryParam("name", 123)
                        }.When {
                            get("/query")
                        }.Then {
                            statusCode(200)
                            body("message", equalTo(123))
                            body("name", equalTo("no-prefix-api"))
                        }
                    }

                    // 패스 파라미터
                    scenario("[GET] /path/{name} 패스 파라미터 호출 시 해당 값이 포함된 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/path/{id}/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "GET",
                            item = noPrefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(noPrefixItem.port)
                        stubFor(
                            get(urlPathMatching("/path/.*"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withTransformers("response-template")
                                        .withBody("""{"message": "{{request.pathSegments.[1]}}", "name": "no-prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                        }.When {
                            get("/path/testValue")
                        }.Then {
                            statusCode(200)
                            body("message", equalTo("testValue"))
                            body("name", equalTo("no-prefix-api"))
                        }
                    }
                }

                feature("prefix 있는 경우") {
                    val prefixItem = prefixItem()
                    scenario("[GET] /prefix/get 엔드포인트 호출 시 200 응답을 반환한다") {
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
                            get(urlEqualTo("/get"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""{"name": "prefix-api"}""")
                                )
                        )

                        // 3. 게이트웨이 호출 및 검증
                        Given {
                            port(port)
                        }.When {
                            get("/prefix/get")
                        }.Then {
                            statusCode(200)
                            body("name", equalTo("prefix-api"))
                        }
                    }

                    // HTTP 메서드들
                    scenario("[POST] /prefix/post 엔드포인트 호출 시 200 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "POST",
                            item = prefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(prefixItem.port)
                        stubFor(
                            post(urlEqualTo("/post"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""{"name": "prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                            contentType("application/json")
                        }.When {
                            post("/prefix/post")
                        }.Then {
                            statusCode(200)
                            body("name", equalTo("prefix-api"))
                        }
                    }

                    scenario("[PUT] /prefix/put 엔드포인트 호출 시 200 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "PUT",
                            item = prefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(prefixItem.port)
                        stubFor(
                            put(urlEqualTo("/put"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""{"name": "prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                            contentType("application/json")
                        }.When {
                            put("/prefix/put")
                        }.Then {
                            statusCode(200)
                            body("name", equalTo("prefix-api"))
                        }
                    }

                    scenario("[DELETE] /prefix/delete 엔드포인트 호출 시 200 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "DELETE",
                            item = prefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(prefixItem.port)
                        stubFor(
                            delete(urlEqualTo("/delete"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""{"name": "prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                        }.When {
                            delete("/prefix/delete")
                        }.Then {
                            statusCode(200)
                            body("name", equalTo("prefix-api"))
                        }
                    }

                    // 쿼리 파라미터
                    scenario("[GET] /prefix/query 쿼리 파라미터 없이 호출 시 message가 null인 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/query/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "GET",
                            item = prefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(prefixItem.port)
                        stubFor(
                            get(urlPathEqualTo("/query"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withTransformers("response-template")
                                        .withBody("""{"message": {{#if request.query.name}}{{request.query.name}}{{else}}null{{/if}}, "name": "prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                        }.When {
                            get("/prefix/query")
                        }.Then {
                            statusCode(200)
                            body("message", nullValue())
                            body("name", equalTo("prefix-api"))
                        }
                    }

                    scenario("[GET] /prefix/query?name=123 쿼리 파라미터와 함께 호출 시 해당 값이 포함된 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/query/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "GET",
                            item = prefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(prefixItem.port)
                        stubFor(
                            get(urlPathEqualTo("/query"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withTransformers("response-template")
                                        .withBody("""{"message": {{#if request.query.name}}{{request.query.name}}{{else}}null{{/if}}, "name": "prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                            queryParam("name", 123)
                        }.When {
                            get("/prefix/query")
                        }.Then {
                            statusCode(200)
                            body("message", equalTo(123))
                            body("name", equalTo("prefix-api"))
                        }
                    }

                    // 패스 파라미터
                    scenario("[GET] /prefix/path/{name} 패스 파라미터 호출 시 해당 값이 포함된 응답을 반환한다") {
                        val targetPath = Path(
                            id = 1L,
                            path = "/path/{id}/**",
                            enableAuth = false,
                            priority = 1,
                            _httpMethod = "GET",
                            item = prefixItem
                        )
                        insertPath(targetPath)
                        applicationEventPublisher.publishEvent(RefreshRoutesEvent(targetPath))

                        configureFor(prefixItem.port)
                        stubFor(
                            get(urlPathMatching("/path/.*"))
                                .willReturn(
                                    aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withTransformers("response-template")
                                        .withBody("""{"message": "{{request.pathSegments.[1]}}", "name": "prefix-api"}""")
                                )
                        )

                        Given {
                            port(port)
                        }.When {
                            get("/prefix/path/testValue")
                        }.Then {
                            statusCode(200)
                            body("message", equalTo("testValue"))
                            body("name", equalTo("prefix-api"))
                        }
                    }
                }

            }
        }
    }
}