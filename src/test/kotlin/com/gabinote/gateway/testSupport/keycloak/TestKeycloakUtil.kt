package com.gabinote.gateway.testSupport.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.json.JSONObject
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.PartialImportRepresentation
import org.keycloak.representations.idm.RealmRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestComponent
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate


private val logger = KotlinLogging.logger {}

@TestComponent
class TestKeycloakUtil(
    private val restTemplate: RestTemplate = RestTemplate(),
) {
    @Value("\${test-keycloak.admin-client.server-url}")
    private lateinit var authServerUrl: String

    @Value("\${test-keycloak.admin-client.realm}")
    private lateinit var realm: String

    @Value("\${test-keycloak.admin-client.id}")
    private lateinit var adminId: String

    @Value("\${test-keycloak.admin-client.password}")
    private lateinit var adminPassword: String


    @Value("\${keycloak.admin-client.realm}")
    private lateinit var applicationRealm: String

    @Value("\${test-keycloak.test-client.id}")
    private lateinit var applicationClientId: String

    @Value("\${test-keycloak.test-client.secret}")
    private lateinit var applicationClientSecret: String

    val mapper = ObjectMapper().registerModule(kotlinModule())

    private val testKeycloak: Keycloak by lazy {
        KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm(realm)
            .grantType(OAuth2Constants.PASSWORD)
            .clientId("admin-cli")
            .username(adminId)
            .password(adminPassword)
            .build()
    }

    fun getAccessToken(testUser: TestUser): String {
        //run with testKeycloak admin client
        if (testUser == TestUser.INVALID) return "invalid"
        val tokenUrl = "$authServerUrl/realms/$applicationRealm/protocol/openid-connect/token"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            add("Accept", MediaType.APPLICATION_JSON_VALUE)
        }
        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "password")
            add("client_id", applicationClientId)
            add("client_secret", applicationClientSecret)
            add("username", testUser.id)
            add("password", testUser.password)
            add("scope", "openid email profile")
        }
        val response = restTemplate.postForEntity(
            tokenUrl,
            HttpEntity(body, headers),
            String::class.java
        )
        if (response.statusCode == HttpStatus.OK) {
            val json = JSONObject(response.body)
            return json.getString("access_token")
        } else {
            throw kotlin.RuntimeException("Failed to get access token: ${response.body}")
        }
    }

    fun recreateRealm() {
        // Keycloak에서 realm을 삭제하고 다시 생성하는 로직
        try {
            testKeycloak.realms().realm(applicationRealm).remove()
            logger.info { "Realm $realm deleted successfully." }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to delete realm $realm, it may not exist." }
        }

        // class path 의 keycloak/realm-export.json 파일을 사용하여 realm을 다시 생성
        try {
            val resource = ClassPathResource("keycloak/realm-export.json")
            val inputStream = resource.inputStream

            val realm: RealmRepresentation = mapper.readValue(inputStream)

            // Realm 생성
            testKeycloak.realms().create(realm)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create realm $realm." }
            throw e
        }
    }

    fun recreateUser() {
        try {
            val resource = ClassPathResource("keycloak/realm-export.json")
            val inputStream = resource.inputStream

            // JSON을 RealmRepresentation으로 변환
            val realmRepresentation: RealmRepresentation = mapper.readValue(inputStream)
            val partialImport = PartialImportRepresentation().apply {
                users = realmRepresentation.users
                ifResourceExists = "OVERWRITE"
            }


            // Realm 생성
            testKeycloak.realms().realm(applicationRealm).partialImport(partialImport)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create realm $realm." }
            throw e
        }
    }

    fun getUser(sub: String): UserRepresentation {
        try {
            val user = testKeycloak.realm(applicationRealm).users().get(sub).toRepresentation()
            logger.info { "User $sub retrieved successfully: $user" }
            return user
        } catch (e: Exception) {
            logger.error(e) { "Failed to retrieve user $sub." }
            throw e
        }
    }

    fun validationUserGroup(
        sub: String,
        groupName: String,
    ): Boolean {
        try {
            val user = getUser(sub)
            val groups = testKeycloak.realm(applicationRealm).users().get(user.id).groups()
            return groups.any { it.name == groupName }
        } catch (e: Exception) {
            logger.error(e) { "Failed to validate user group for $sub." }
            throw e
        }
    }

    fun validationUserExist(
        sub: String,
        negativeMode: Boolean = false
    ): Boolean {
        return try {
            getUser(sub)
            if (negativeMode) {
                logger.info { "User $sub exists, but negative mode is enabled." }
                false
            } else {
                logger.info { "User $sub exists." }
                true
            }
        } catch (e: Exception) {
            if (negativeMode) {
                logger.info { "User $sub does not exist, as expected in negative mode." }
                true
            } else {
                logger.error(e) { "Failed to validate existence of user $sub." }
                false
            }
        }
    }

}