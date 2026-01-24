package com.gabinote.gateway.testSupport.keycloak

data class TestTokenRes(
    val accessToken: String,
    val refreshToken: String,
)
