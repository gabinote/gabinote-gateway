package com.gabinote.gateway.domain.path

import com.gabinote.gateway.domain.item.Item
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.http.HttpMethod

@Table("GATEWAY_PATH")
data class Path(
    @Id
    @Column("GATEWAY_PATH_PK")
    val id: Long,

    @Column("GATEWAY_PATH_PATH")
    val path: String,

    @Column("GATEWAY_PATH_PRIORITY")
    val priority: Int,

    @Column("GATEWAY_PATH_ENABLE_AUTH")
    val enableAuth: Boolean,

    @Column("GATEWAY_PATH_ROLE")
    val role: String? = null,

    @Column("GATEWAY_PATH_HTTP_METHOD")
    val _httpMethod: String,

    @Column("GATEWAY_ITEM_PK")
    val item: Item,

    @Column("GATEWAY_PATH_IS_ENABLED")
    val isEnabled: Boolean = true,

    @Column("GATEWAY_PATH_RATE_LIMIT_REPLENISH")
    val replenishRate: Int? = null,

    @Column("GATEWAY_PATH_RATE_LIMIT_BURST")
    val burstCapacity: Int? = null,

    ) {
    val httpMethod: HttpMethod
        get() = HttpMethod.valueOf(_httpMethod.uppercase())
}