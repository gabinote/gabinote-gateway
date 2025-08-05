package com.gabinote.gateway.service

import com.gabinote.gateway.domain.path.PathRepository
import com.gabinote.gateway.dto.path.service.PathSimpleResServiceDto
import com.gabinote.gateway.mapper.path.PathMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux

@Service
class PathService(
    private val pathRepository: PathRepository
) {

    @Transactional(readOnly = true)
    fun getAllPath(): Flux<PathSimpleResServiceDto> {
        return pathRepository.findAllPath().map { path -> PathMapper.toServiceDto(path) }
    }
}