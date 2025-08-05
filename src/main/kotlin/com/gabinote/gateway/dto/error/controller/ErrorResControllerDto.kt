package com.gabinote.gateway.dto.error.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

//TODO ProblemDetail RFC 7807
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ErrorResControllerDto(
    val httpCode: Int,
    val errorCode: String,
    val message: String,
    val clientDetail: List<String>,
    val date: String,
    val path: String,
)
