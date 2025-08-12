package com.gabinote.gateway.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cloud.gateway.event.RefreshRoutesEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@RequestMapping("/api/v1/management")
@RestController
class ManagementApiController(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @PostMapping("/refresh")
    fun refreshGateway(
        @AuthenticationPrincipal jwt: Jwt
    ): Mono<ResponseEntity<String>> {
        logger.info { "${LocalDateTime.now()} Received request to refresh gateway routes by ${jwt.subject}." }
        applicationEventPublisher.publishEvent(RefreshRoutesEvent(this))
        logger.info { "${LocalDateTime.now()} Published RefreshRoutesEvent to refresh gateway routes by ${jwt.subject}." }
        return Mono.just(ResponseEntity.ok("Gateway routes refreshed successfully."))
    }
}