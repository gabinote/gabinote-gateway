package com.gabinote.gateway.util.rateLimit

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class RateLimiterFactory(
    // [변경] 템플릿, 스크립트 다 필요 없고 '컨텍스트' 하나면 됩니다.
    private val applicationContext: ApplicationContext,
) {

    fun create(replenishRate: Int, burstCapacity: Int): RedisRateLimiter {

        // 1. 깡통 객체 생성 (설정값만 들고 있음)
        val limiter = RedisRateLimiter(replenishRate, burstCapacity)

        // 2. [핵심] "자, 여기 스프링 컨텍스트 열쇠다. 필요한 거 알아서 꺼내 써라."
        // 이 메서드가 호출되는 순간, 내부에서 redisTemplate과 script를 자동으로 찾아 연결합니다.
        limiter.setApplicationContext(applicationContext)

        return limiter
    }
}