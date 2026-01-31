package com.gabinote.gateway.testSupport.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 테스트 환경에서 Redis 데이터를 초기화하기 위한 헬퍼 클래스
 */
@Component
class TestRedisHelper(
    private val connectionFactory: ReactiveRedisConnectionFactory,
    private val redisTemplate: ReactiveStringRedisTemplate,
) {

    /**
     * Redis의 모든 데이터를 삭제합니다 (FLUSHALL)
     * 모든 데이터베이스의 데이터를 삭제합니다.
     */
    fun flushAll() {
        logger.debug { "Flushing all Redis data..." }
        connectionFactory.reactiveConnection.serverCommands().flushAll().block()
        logger.debug { "All Redis data flushed." }

    }
}
