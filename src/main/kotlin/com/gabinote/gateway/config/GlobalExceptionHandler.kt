package com.gabinote.gateway.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.gateway.dto.error.controller.ErrorResControllerDto
import com.gabinote.gateway.util.exception.AuthenticationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { }

@Component
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper
) : ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        val request = exchange.request
        val requestId = request.headers.getFirst("X-Request-Id") ?: "unknown"
        response.headers.contentType = MediaType.APPLICATION_JSON
        logger.info { "[ExceptionHandler] ID : $requestId failed. ${ex.message}(${ex.javaClass.simpleName})" }
        val path = request.path.value()

        val errorResponse = when (ex) {
            is ResponseStatusException -> ErrorResControllerDto(
                errorCode = "G_NOT_FOUND",
                message = "cannot find the requested resource",
                path = path,
                date = LocalDateTime.now().toString(),
                clientDetail = listOf("cannot find the requested resource"),
                httpCode = 404
            )

            is AuthenticationException, is InvalidBearerTokenException, is AccessDeniedException -> ErrorResControllerDto(
                errorCode = "G_AUTHENTICATION_ERROR",
                message = "Authentication failed",
                path = path,
                date = LocalDateTime.now().toString(),
                clientDetail = listOf("Authentication failed due to invalid credentials"),
                httpCode = 401
            )

            // 그외 오류는 ErrorCode.UNDEFINED_ERROR 기반으로 메시지를 사용
            else -> ErrorResControllerDto(
                errorCode = "G_UNDEFINED_ERROR",
                message = "An unexpected error occurred",
                path = path,
                date = LocalDateTime.now().toString(),
                clientDetail = listOf("An unexpected error occurred"),
                httpCode = 500
            )
        }
        response.statusCode = HttpStatus.valueOf(errorResponse.httpCode)
        return response.writeWith(
            Mono.just(
                response.bufferFactory().wrap(
                    objectMapper.writeValueAsBytes(errorResponse)
                )
            )
        )

    }
}