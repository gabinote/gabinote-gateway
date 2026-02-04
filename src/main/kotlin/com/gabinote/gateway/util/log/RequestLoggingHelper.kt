package com.gabinote.gateway.util.log

import org.springframework.http.server.reactive.ServerHttpRequest

object RequestLoggingHelper {
    fun build(request: ServerHttpRequest, requestId: String): String {
        val method = request.method
        val path = request.uri.path
        val queryParams = request.queryParams
        val headers = request.headers
        val remoteAddress = request.remoteAddress?.address?.hostAddress ?: "Unknown"

        return "Request RequestId=($requestId) [$method] Path=($path) RemoteAddress=($remoteAddress), QueryParams=($queryParams), Headers=($headers)"

    }
}