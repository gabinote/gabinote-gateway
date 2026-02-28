package com.gabinote.gateway.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gateway.cors")
data class CorsProperties(
    val allowedOrigins: Set<String>,
    val defaultAllowedOrigin: String,
)