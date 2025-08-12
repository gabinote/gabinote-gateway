package com.gabinote.gateway.domain.path

import com.gabinote.gateway.domain.item.Item
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.http.HttpMethod

@Table("GATEWAY_PATH")
class Path(
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
    val item: Item

) {
    val httpMethod: HttpMethod
        get() = HttpMethod.valueOf(_httpMethod.uppercase())

    override fun toString(): String {
        return "Path(id=$id, path='$path', enableAuth=$enableAuth, role='$role', httpMethod=$httpMethod, item=${item.id}, priority=${priority})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Path

        return id != other.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + priority
        result = 31 * result + enableAuth.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + _httpMethod.hashCode()
        result = 31 * result + item.hashCode()
        result = 31 * result + httpMethod.hashCode()
        return result
    }
}