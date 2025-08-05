package com.gabinote.gateway.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class IndexController {

    @GetMapping("/gateway/ping")
    fun ping(): Flux<String> {
        return Flux.just("PONG")
    }


}