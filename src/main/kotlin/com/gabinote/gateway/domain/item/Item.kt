package com.gabinote.gateway.domain.item

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("GATEWAY_ITEM")
class Item(
    @Id
    @Column("GATEWAY_ITEM_PK")
    val id: Long,

    @Column("GATEWAY_ITEM_NAME")
    val name: String,

    @Column("GATEWAY_ITEM_URL")
    val url: String,

    @Column("GATEWAY_ITEM_PORT")
    val port: Int,

    @Column("GATEWAY_ITEM_PREFIX")
    val prefix: String? = null,
)