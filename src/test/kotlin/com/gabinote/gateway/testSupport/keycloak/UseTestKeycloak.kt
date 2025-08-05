package com.gabinote.gateway.testSupport.keycloak

import org.springframework.test.context.ContextConfiguration
import java.lang.annotation.Inherited


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@ContextConfiguration(initializers = [KeycloakContainerInitializer::class])
annotation class UseTestKeycloak