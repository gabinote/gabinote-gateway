package com.gabinote.gateway.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gateway.headers")
data class HeaderProperties(

    val subHeader: String,

    val rolesHeader: String,

    val secretHeader: String,

    val requestIdHeader: String,
)