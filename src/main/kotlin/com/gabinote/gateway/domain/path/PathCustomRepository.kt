package com.gabinote.gateway.domain.path

import reactor.core.publisher.Flux

interface PathCustomRepository {
    fun findAllPath(): Flux<Path>
    fun findAllPathByItemId(id: Int): Flux<Path>
}