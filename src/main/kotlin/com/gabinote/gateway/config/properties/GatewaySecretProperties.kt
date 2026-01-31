package com.gabinote.gateway.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gateway.secret")
data class GatewaySecretProperties(val secretKey: String)