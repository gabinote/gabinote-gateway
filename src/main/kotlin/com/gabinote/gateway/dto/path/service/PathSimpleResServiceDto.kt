package com.gabinote.gateway.dto.path.service

import com.gabinote.gateway.dto.item.service.ItemResServiceDto
import org.springframework.http.HttpMethod


data class PathSimpleResServiceDto(
    val path: String,
    val enableAuth: Boolean,
    val role: String? = null,
    val httpMethod: HttpMethod,
    val item: ItemResServiceDto,

    ) {
    fun itemUrl(): String {
        return "${item.url}:${item.port}"
    }

}