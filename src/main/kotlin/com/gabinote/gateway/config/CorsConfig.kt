package com.gabinote.gateway.config

import com.gabinote.gateway.config.properties.CorsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class CorsConfig