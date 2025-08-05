package com.gabinote.gateway.mapper.path

import com.gabinote.gateway.domain.path.Path
import com.gabinote.gateway.dto.path.service.PathSimpleResServiceDto
import com.gabinote.gateway.mapper.item.ItemMapper

object PathMapper {

    fun toServiceDto(path: Path): PathSimpleResServiceDto {
        return PathSimpleResServiceDto(
            enableAuth = path.enableAuth,
            role = path.role,
            item = ItemMapper.toServiceDto(path.item),
            httpMethod = path.httpMethod,
            path = path.path
        )
    }
}