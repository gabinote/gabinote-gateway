package com.gabinote.gateway.testSupport.kotest

import io.kotest.core.extensions.SpecExtension
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.spring.Properties
import io.kotest.extensions.spring.SpringTestContextCoroutineContextElement
import io.kotest.extensions.spring.testContextManager
import kotlinx.coroutines.withContext
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.FixedValue
import org.springframework.test.context.TestContextManager
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import kotlin.reflect.KClass

//ref : https://effortguy.tistory.com/476
class SpringTestContextModeExtension : TestCaseExtension, SpecExtension {

    private var ignoreSpringListenerOnFinalClassWarning: Boolean = false

    override suspend fun intercept(spec: Spec, execute: suspend (Spec) -> Unit) {
        safeClassName(spec::class)

        val context = TestContextManager(spec::class.java)
        withContext(SpringTestContextCoroutineContextElement(context)) {
            testContextManager().beforeTestClass()
            testContextManager().prepareTestInstance(spec)
            execute(spec)
            testContextManager().afterTestClass()
        }
    }

    override suspend fun intercept(testCase: TestCase, execute: suspend (TestCase) -> TestResult): TestResult {
        val methodName = method(testCase)
        if (testCase.isApplicable()) {
            testContextManager().beforeTestMethod(testCase.spec, methodName)
            testContextManager().beforeTestExecution(testCase.spec, methodName)
        }
        val result = execute(testCase)
        if (testCase.isApplicable()) {
            testContextManager().afterTestMethod(testCase.spec, methodName, null as Throwable?)
            testContextManager().afterTestExecution(testCase.spec, methodName, null as Throwable?)
        }
        return result
    }

    private fun TestCase.isApplicable() = name.prefix == "Context: "

    private fun method(testCase: TestCase): Method = if (Modifier.isFinal(testCase.spec::class.java.modifiers)) {
        if (!ignoreFinalWarning) {
            @Suppress("MaxLineLength")
            println("Using SpringListener on a final class. If any Spring annotation fails to work, try making this class open.")
        }
        // the method here must exist since we can't add our own
        this@SpringTestContextModeExtension::class.java.methods.firstOrNull { it.name == "intercept" }
            ?: error("Could not find method 'intercept' to attach spring lifecycle methods to")
    } else {
        val methodName = methodName(testCase)
        val fakeSpec = ByteBuddy()
            .subclass(testCase.spec::class.java)
            .defineMethod(methodName, String::class.java, Visibility.PUBLIC)
            .intercept(FixedValue.value("Foo"))
            .make()
            .load(this::class.java.classLoader, ClassLoadingStrategy.Default.CHILD_FIRST)
            .loaded
        fakeSpec.getMethod(methodName)
    }

    private fun safeClassName(kclass: KClass<*>) {
        if (kclass.java.name.split('.').any { illegals.contains(it) })
            error("Spec package name cannot contain a java keyword: ${illegals.joinToString(",")}")
    }

    private fun methodName(testCase: TestCase): String = (testCase.name.testName + "_" + UUID.randomUUID().toString())
        .replace(methodNameRegex, "_")
        .let {
            if (it.first().isLetter()) it else "_$it"
        }

    private val illegals =
        listOf("import", "finally", "catch", "const", "final", "inner", "protected", "private", "public")

    private val methodNameRegex = "[^a-zA-Z_0-9]".toRegex()

    private val ignoreFinalWarning =
        ignoreSpringListenerOnFinalClassWarning ||
                !System.getProperty(Properties.springIgnoreWarning, "false").toBoolean()
}