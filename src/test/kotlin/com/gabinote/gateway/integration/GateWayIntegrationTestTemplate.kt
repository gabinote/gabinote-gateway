package com.gabinote.gateway.integration

import com.gabinote.gateway.domain.item.Item
import com.gabinote.gateway.domain.item.ItemRepository
import com.gabinote.gateway.domain.path.Path
import com.gabinote.gateway.domain.path.PathRepository
import com.gabinote.gateway.testSupport.db.testDb.DatabaseContainerInitializer
import com.gabinote.gateway.testSupport.keycloak.KeycloakContainerInitializer
import com.gabinote.gateway.testSupport.keycloak.TestKeycloakUtil
import com.gabinote.gateway.testSupport.redis.RedisContainerInitializer
import com.gabinote.gateway.testSupport.stubServer.TestStubServerContainerInitializer
import io.kotest.core.spec.style.FeatureSpec
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig.encoderConfig
import io.restassured.parsing.Parser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Import
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers


@Import(TestKeycloakUtil::class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [DatabaseContainerInitializer::class, KeycloakContainerInitializer::class, TestStubServerContainerInitializer::class, RedisContainerInitializer::class])
class GateWayIntegrationTestTemplate : FeatureSpec() {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Autowired
    lateinit var databaseClient: DatabaseClient

    @Autowired
    lateinit var testKeycloakUtil: TestKeycloakUtil

    @Value("\${test.api.prefix-port}")
    lateinit var prefixApiPort: String

    @Value("\${test.api.no-prefix-port}")
    lateinit var noPrefixApiPort: String

    @Autowired
    lateinit var itemRepository: ItemRepository

    @Autowired
    lateinit var pathRepository: PathRepository


    init {
        beforeSpec {
            setupRestAssured()
        }

        beforeTest {
            reset()
            cleanDB()
            setupBaseApiItem()
        }
    }

    protected fun noPrefixItem(): Item {
        return Item(
            id = 1L,
            name = "no-prefix-api",
            url = "http://localhost",
            port = noPrefixApiPort.toInt(),
            prefix = null
        )
    }

    protected fun prefixItem(): Item {
        return Item(
            id = 2L,
            name = "prefix-api",
            url = "http://localhost",
            port = prefixApiPort.toInt(),
            prefix = "prefix"
        )
    }

    private fun setupBaseApiItem() {

        insertItem(prefixItem())
        insertItem(noPrefixItem())
    }

    private fun insertItem(item: Item) {
        val sql = """
        INSERT INTO GATEWAY_ITEM (GATEWAY_ITEM_PK, GATEWAY_ITEM_NAME, GATEWAY_ITEM_URL, GATEWAY_ITEM_PORT, GATEWAY_ITEM_PREFIX)
        VALUES (:id, :name, :url, :port, :prefix)
    """.trimIndent()
        var spec = databaseClient.sql(sql)
            .bind("id", item.id)
            .bind("name", item.name)
            .bind("url", item.url)
            .bind("port", item.port)

        spec = if (item.prefix != null) {
            spec.bind("prefix", item.prefix)
        } else {
            spec.bindNull("prefix", String::class.java)
        }

        spec.fetch()
            .rowsUpdated()
            .block()
    }

    private fun setupRestAssured() {
        RestAssured.defaultParser = Parser.JSON
        RestAssured.config = RestAssured.config()
            .encoderConfig(
                encoderConfig()
                    .defaultContentCharset("UTF-8")
            )
    }

    private fun cleanDB() {
        databaseClient.sql("SET FOREIGN_KEY_CHECKS = 0")
            .then()
            .then(databaseClient.sql("TRUNCATE TABLE GATEWAY_PATH").then())
            .then(databaseClient.sql("TRUNCATE TABLE GATEWAY_ITEM").then())
            .then(databaseClient.sql("SET FOREIGN_KEY_CHECKS = 1").then())
            .block()
    }

    fun insertPath(path: Path) {
        // 1. 초기 스펙 생성 및 공통 필드 바인딩
        var spec = databaseClient.sql(
            """
        INSERT INTO GATEWAY_PATH (
            GATEWAY_PATH_PK, GATEWAY_PATH_PATH, GATEWAY_PATH_PRIORITY, GATEWAY_PATH_ENABLE_AUTH, 
            GATEWAY_PATH_ROLE, GATEWAY_PATH_HTTP_METHOD, GATEWAY_ITEM_PK, GATEWAY_PATH_IS_ENABLED,
            GATEWAY_PATH_RATE_LIMIT_REPLENISH, GATEWAY_PATH_RATE_LIMIT_BURST
        ) VALUES (
            :id, :path, :priority, :enableAuth, :role, :httpMethod, :itemId, :isEnabled, :rateLimitReplenish, :rateLimitBurst
        )
    """.trimIndent()
        )
            .bind("id", path.id)
            .bind("path", path.path)
            .bind("priority", path.priority)
            .bind("enableAuth", path.enableAuth)
            .bind("httpMethod", path._httpMethod)
            .bind("itemId", path.item.id)
            .bind("isEnabled", path.isEnabled)

        // 2. Nullable 필드(role) 조건부 바인딩
        spec = if (path.role != null) {
            spec.bind("role", path.role)
        } else {
            spec.bindNull("role", String::class.java)
        }

        spec = if (path.replenishRate != null) {
            spec.bind("rateLimitReplenish", path.replenishRate)
        } else {
            spec.bindNull("rateLimitReplenish", Integer::class.java)
        }

        spec = if (path.burstCapacity != null) {
            spec.bind("rateLimitBurst", path.burstCapacity)
        } else {
            spec.bindNull("rateLimitBurst", Integer::class.java)
        }

        // 3. 실행
        spec.fetch()
            .rowsUpdated()
            .block()
    }


    private fun reset() {
        // WireMockServer 인스턴스를 직접 사용하여 reset (더 안정적)
        TestStubServerContainerInitializer.firApi.resetAll()
        TestStubServerContainerInitializer.secApi.resetAll()
    }


}

