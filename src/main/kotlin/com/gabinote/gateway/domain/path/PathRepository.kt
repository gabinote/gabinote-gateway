package com.gabinote.gateway.domain.path

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PathRepository : ReactiveCrudRepository<Path, Long>, PathCustomRepository