package com.gabinote.gateway.testSupport.testApi

import org.springframework.test.context.ContextConfiguration
import java.lang.annotation.Inherited


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@ContextConfiguration(initializers = [TestApiContainerInitializer::class])
annotation class UseTestApi