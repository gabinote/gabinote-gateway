package com.gabinote.gateway.domain.item


import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import org.springframework.stereotype.Component
import java.util.function.BiFunction



@Component
class ItemEntityMapper : BiFunction<Row, RowMetadata, Item> {
    override fun apply(t: Row, u: RowMetadata): Item {
        return Item(
            id = t.get("GATEWAY_ITEM_PK", Long::class.java) ?: throw IllegalArgumentException("ID cannot be null"),
            name = t.get("GATEWAY_ITEM_NAME", String::class.java) ?: throw  IllegalArgumentException("Name cannot be null"),
            url = t.get("GATEWAY_ITEM_URL", String::class.java) ?: throw  IllegalArgumentException("URL cannot be null"),
            port = t.get("GATEWAY_ITEM_PORT", Int::class.java) ?: throw IllegalArgumentException("Port cannot be null"),
        )
    }
}