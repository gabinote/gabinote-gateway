package com.gabinote.gateway.config

import com.gabinote.gateway.config.properties.HeaderProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(HeaderProperties::class)
class HeaderConfig