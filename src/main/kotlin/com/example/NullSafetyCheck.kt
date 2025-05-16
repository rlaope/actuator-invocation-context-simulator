package com.example

import org.springframework.boot.actuate.endpoint.InvocationContext
import org.springframework.boot.actuate.endpoint.OperationArgumentResolver

class DummyContext : InvocationContext {
    override fun getSecurityContext(): Any = TODO()
    override fun getArguments(): Map<String, Any> = emptyMap()
    override fun resolveArgument(argumentType: Class<*>): Any? = null
    override fun <T> resolveArgument(argumentType: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return resolveArgument(argumentType) as T
    }

    override fun getParameters(): Map<String, Any> = emptyMap()
    override fun getOperationArgumentResolvers(): List<OperationArgumentResolver> = emptyList()
}

fun main() {
    val context = DummyContext()
    val result = context.resolveArgument(String::class.java)
    val length = result.length  // <- 여기서 null 가능성 경고 없음
    println("Length: $length")
}
