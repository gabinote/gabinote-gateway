package com.gabinote.gateway.testSupport.keycloak

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

private val logger = KotlinLogging.logger {}

class KeycloakContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        @JvmStatic
        val keycloak: KeycloakContainer = KeycloakContainer("quay.io/keycloak/keycloak:latest")
            .withRealmImportFile("keycloak/realm-export.json")
            .withLabel("group", "test-keycloak")
            .withAdminPassword("admin")
            .withAdminUsername("admin")
            .withReuse(true)
    }


    override fun initialize(context: ConfigurableApplicationContext) {
        // 테스트 컨테이너 시작
        keycloak.start()
        logger.debug { "Keycloak started on port ${keycloak.httpPort},url ${keycloak.authServerUrl}" }
        TestPropertyValues.of(
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=${keycloak.authServerUrl}/realms/gabinote-test",
            "test-keycloak.admin-client.server-url=${keycloak.authServerUrl}",
            "test-keycloak.admin-client.id=${keycloak.adminUsername}",
            "test-keycloak.admin-client.password=${keycloak.adminPassword}"
        ).applyTo(context.environment)


    }


}