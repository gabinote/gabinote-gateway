package com.gabinote.gateway.testSupport.stubServer

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*

object TestStubServerHelper {
    fun setupStubs(server: WireMockServer, appName: String) {
        // GET / - default endpoint
        server.stubFor(
            get(urlEqualTo("/"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"name": "$appName"}""")
                )
        )

        // GET /query - query parameter endpoint
        server.stubFor(
            get(urlPathEqualTo("/query"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"message": null, "name": "$appName"}""")
                )
        )

        server.stubFor(
            get(urlPathMatching("/query\\?name=.*"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("response-template")
                        .withBody("""{"message": {{request.query.name}}, "name": "$appName"}""")
                )
        )

        // GET /path/{name} - path parameter endpoint
        server.stubFor(
            get(urlPathMatching("/path/.*"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("response-template")
                        .withBody("""{"message": "{{request.pathSegments.[1]}}", "name": "$appName"}""")
                )
        )

        // GET /need-auth - authentication required endpoint
        server.stubFor(
            get(urlEqualTo("/need-auth"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("response-template")
                        .withBody("""{"sub": "{{request.headers.X-Token-Sub}}", "role": {{#if request.headers.X-Token-Role}}["{{request.headers.X-Token-Role}}"]{{else}}null{{/if}}, "name": "$appName"}""")
                )
        )

        // GET /optional-auth - optional authentication endpoint
        server.stubFor(
            get(urlEqualTo("/optional-auth"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("response-template")
                        .withBody("""{"sub": "{{request.headers.X-Token-Sub}}", "role": {{#if request.headers.X-Token-Role}}["{{request.headers.X-Token-Role}}"]{{else}}null{{/if}}, "name": "$appName"}""")
                )
        )
    }
}