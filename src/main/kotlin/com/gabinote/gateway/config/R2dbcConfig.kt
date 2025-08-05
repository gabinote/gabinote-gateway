package com.gabinote.gateway.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@EnableR2dbcRepositories(basePackages = ["com.gabinote.gateway.domain"])
class R2dbcConfig(private val connectionFactory: ConnectionFactory) {

    @Bean
    fun databaseClient(): DatabaseClient {
        return DatabaseClient.create(connectionFactory)
    }

}