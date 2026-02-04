package com.gabinote.gateway.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.gateway.util.exception.AuthenticationException
import com.gabinote.gateway.util.log.ExceptionAdviceHelper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger { }

@Component
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper,
) : ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        val request = exchange.request
        val requestId = request.headers.getFirst("X-Request-Id") ?: "unknown"
        response.headers.contentType = MediaType.APPLICATION_JSON
        logger.info { "request failed. request Id = ($requestId) error name = (${ex.javaClass.simpleName}) message = (${ex.message}) " }
        request.path.value()

        val errorResponse = when (ex) {
            is ResponseStatusException -> ExceptionAdviceHelper.problemDetail(
                status = HttpStatus.BAD_REQUEST,
                title = "Gateway Response Status Exception",
                detail = ex.message,
                requestId = requestId,
            )

            is AuthenticationException, is InvalidBearerTokenException, is AccessDeniedException -> ExceptionAdviceHelper.problemDetail(
                status = HttpStatus.UNAUTHORIZED,
                title = "Gateway Authentication Failed",
                detail = "Authentication failed for the request.",
                requestId = requestId,
            )

            // 그외 오류는 ErrorCode.UNDEFINED_ERROR 기반으로 메시지를 사용
            else -> ExceptionAdviceHelper.problemDetail(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                title = "Gateway Undefined Error",
                detail = "An undefined error occurred while processing the request.",
                requestId = requestId,
            )
        }
        response.statusCode = HttpStatus.valueOf(errorResponse.status)
        return response.writeWith(
            Mono.just(
                response.bufferFactory().wrap(
                    objectMapper.writeValueAsBytes(errorResponse)
                )
            )
        )

    }
}