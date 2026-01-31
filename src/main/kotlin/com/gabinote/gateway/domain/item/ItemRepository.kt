package com.gabinote.gateway.domain.item

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : ReactiveCrudRepository<Item, Long>