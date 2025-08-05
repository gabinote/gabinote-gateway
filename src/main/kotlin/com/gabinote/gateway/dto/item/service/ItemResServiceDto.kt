package com.gabinote.gateway.dto.item.service

data class ItemResServiceDto(
    val id: Long,
    val name: String,
    val url: String,
    val port: Int,
    val prefix: String? = null,
)