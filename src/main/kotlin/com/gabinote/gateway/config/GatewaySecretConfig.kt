package com.gabinote.gateway.config

import com.gabinote.gateway.config.properties.GatewaySecretProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GatewaySecretProperties::class)
class GatewaySecretConfig(

)