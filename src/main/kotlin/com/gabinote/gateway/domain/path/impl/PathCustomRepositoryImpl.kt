package com.gabinote.gateway.domain.path.impl

import com.gabinote.gateway.domain.path.Path
import com.gabinote.gateway.domain.path.PathCustomRepository
import com.gabinote.gateway.domain.path.PathEntityMapper
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux

class PathCustomRepositoryImpl(
    private val client: DatabaseClient,
    private val pathMapper: PathEntityMapper
) : PathCustomRepository {
    override fun findAllPath(): Flux<Path> {
        val query = """
            SELECT * FROM GATEWAY_PATH as p
                  INNER JOIN GATEWAY_ITEM as i ON i.GATEWAY_ITEM_PK = p.GATEWAY_ITEM_PK
                  WHERE p.GATEWAY_PATH_IS_ENABLED = true
                  ORDER BY p.GATEWAY_PATH_PRIORITY DESC
        """.trimIndent()

        return client.sql { query }
            .map(pathMapper)
            .all()
    }

    override fun findAllPathByItemId(id: Int): Flux<Path> {
        val query = """
            SELECT * FROM GATEWAY_PATH as p
                    INNER JOIN GATEWAY_ITEM as i ON i.GATEWAY_ITEM_PK = p.GATEWAY_ITEM_PK
                    WHERE p.GATEWAY_ITEM_PK = ?
                    ORDER BY p.GATEWAY_PATH_PK DESC;
        """.trimIndent()
        return client.sql { query }
            .bind(0, id)
            .map(pathMapper)
            .all()
    }
}