package com.gabinote.gateway.domain.path

import com.gabinote.gateway.domain.item.Item
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import org.springframework.stereotype.Component
import java.util.function.BiFunction

@Component
class PathEntityMapper : BiFunction<Row, RowMetadata, Path> {
    override fun apply(t: Row, u: RowMetadata): Path {
        return Path(
            id = t.get("GATEWAY_PATH_PK", Long::class.java) ?: throw IllegalArgumentException("Path ID is required"),
            path = t.get("GATEWAY_PATH_PATH", String::class.java) ?: throw IllegalArgumentException("Path is required"),
            role = t.get("GATEWAY_PATH_ROLE", String::class.java),
            _httpMethod = t.get("GATEWAY_PATH_HTTP_METHOD", String::class.java)
                ?: throw IllegalArgumentException("HTTP Method is required"),
            enableAuth = t.get("GATEWAY_PATH_ENABLE_AUTH", Boolean::class.java)
                ?: throw IllegalArgumentException("Enable Auth is required"),
            item = Item(
                id = t.get("GATEWAY_ITEM_PK", Long::class.java)
                    ?: throw IllegalArgumentException("Item ID is required"),
                name = t.get("GATEWAY_ITEM_NAME", String::class.java)
                    ?: throw IllegalArgumentException("Item name is required"),
                url = t.get("GATEWAY_ITEM_URL", String::class.java)
                    ?: throw IllegalArgumentException("Item URL is required"),
                port = t.get("GATEWAY_ITEM_PORT", Int::class.java)
                    ?: throw IllegalArgumentException("Item port is required"),
                prefix = t.get("GATEWAY_ITEM_PREFIX", String::class.java)
            ),
            priority = t.get("GATEWAY_PATH_PRIORITY", Int::class.java) ?: 0,
            isEnabled = t.get("GATEWAY_PATH_IS_ENABLED", Boolean::class.java) ?: true,
            replenishRate = t.get("GATEWAY_PATH_RATE_LIMIT_REPLENISH", Integer::class.java)?.toInt(),
            burstCapacity = t.get("GATEWAY_PATH_RATE_LIMIT_BURST", Integer::class.java)?.toInt(),
        )
    }
}