package com.gabinote.gateway.mapper.item

import com.gabinote.gateway.domain.item.Item
import com.gabinote.gateway.dto.item.service.ItemResServiceDto

object ItemMapper {
    fun toServiceDto(item: Item): ItemResServiceDto {
        return ItemResServiceDto(
            id = item.id,
            name = item.name,
            url = item.url,
            port = item.port,
            prefix = item.prefix,
        )
    }
}