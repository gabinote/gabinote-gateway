package com.gabinote.gateway

import com.gabinote.gateway.testSupport.db.testDb.DatabaseContainerInitializer
import com.gabinote.gateway.testSupport.keycloak.KeycloakContainerInitializer
import com.gabinote.gateway.testSupport.keycloak.TestKeycloakUtil
import com.gabinote.gateway.testSupport.stubServer.TestStubServerContainerInitializer
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

@Import(TestKeycloakUtil::class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [DatabaseContainerInitializer::class, KeycloakContainerInitializer::class, TestStubServerContainerInitializer::class])
class GatewayApplicationTests {

    @Test
    fun contextLoads() {
    }

}
