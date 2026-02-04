package com.gabinote.gateway.util.log

import org.springframework.http.HttpStatusCode
import org.springframework.http.server.reactive.ServerHttpRequest

object ResponseLoggingHelper {
    fun build(request: ServerHttpRequest, statusCode: HttpStatusCode?, requestId: String): String {
        val method = request.method
        val path = request.uri.path
        val queryParams = request.queryParams
        val headers = request.headers
        val remoteAddress = request.remoteAddress?.address?.hostAddress ?: "Unknown"

        return "Response RequestId=($requestId) [$method] Path=($path) RemoteAddress=($remoteAddress), StatusCode=($statusCode), QueryParams=($queryParams), Headers=($headers) Status={$statusCode}"
    }
}