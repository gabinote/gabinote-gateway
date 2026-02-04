package com.gabinote.gateway.util.log

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import java.net.URI

object ExceptionAdviceHelper {

    /**
     * ProblemDetail 객체를 생성하는 메서드
     * RFC 7807 스펙에 맞는 문제 상세 정보 생성
     * @param status HTTP 상태 코드
     * @param title 문제 제목
     * @param detail 문제 상세 설명
     * @param type 문제 타입 URI
     * @param instance 문제 인스턴스 URI
     * @param requestId 요청 ID
     * @param additionalProperties 추가 속성 맵
     * @return 생성된 ProblemDetail 객체
     */
    fun problemDetail(
        status: HttpStatus,
        title: String? = "Unexpected Error",
        detail: String? = null,
        type: URI = URI("about:blank"),
        instance: URI? = null,
        requestId: String? = null,
        additionalProperties: Map<String, Any> = emptyMap(),
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, detail ?: title)
        problemDetail.title = title
        problemDetail.type = type
        problemDetail.instance = instance
        problemDetail.properties = additionalProperties
        return problemDetail
    }
}