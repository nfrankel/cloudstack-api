package com.exoscale.api.generate

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.json.JSONObject
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass

internal const val PACKAGE_NAME = "com.exoscale.api"

internal const val RESULT_CLASS_NAME_SUFFIX = "Result"

internal const val NAME_KEY = "name"
private const val ASYNC_KEY = "isasync"
private const val REQUIRED_KEY = "required"

internal val JSONObject.resultClassName
    get() = "${name.capitalize()}$RESULT_CLASS_NAME_SUFFIX"

internal val JSONObject.name
    get() = getString(NAME_KEY)

internal val JSONObject.isRequired: Boolean
    get()= if (has(REQUIRED_KEY)) getBoolean(REQUIRED_KEY)
           else false

internal val JSONObject.isAsync: Boolean
    get() = if (has(ASYNC_KEY)) getBoolean(ASYNC_KEY)
            else false

internal fun JSONObject.toConstructorParameterSpec(): ParameterSpec {
    val type = computeParameterType()
    return ParameterSpec.builder(getString(NAME_KEY), type)
        .addAnnotation(suppressUnusedWarning)
        .addKdoc("${toDoc()}${System.lineSeparator()}")
        .apply {
        if (type is ParameterizedTypeName) {
            when {
                type.rawType == List::class.asTypeName() -> defaultValue("arrayListOf()")
                type.rawType == Set::class.asTypeName() -> defaultValue("setOf()")
                type.rawType == Map::class.asTypeName() -> defaultValue("mapOf()")
            }
        } else if (type.isNullable) {
            defaultValue("null")
        }
    }.build()
}

internal fun JSONObject.computePropertyType(): TypeName {
    val base: KClass<out Any> = mappings[getString("type")] ?: Nothing::class
    return when (base) {
        List::class, Set::class -> base.parameterizedBy(Any::class)
        Map::class -> base.parameterizedBy(String::class, Any::class)
        else -> base.asTypeName()
    }.copy(true)
}

internal fun JSONObject.computeParameterType(): TypeName {
    val base: KClass<out Any> = mappings[getString("type")] ?: Nothing::class
    return when (base) {
        List::class, Set::class -> base.parameterizedBy(String::class)
        Map::class -> base.parameterizedBy(String::class, String::class)
        else -> base.asTypeName().copy(!isRequired)
    }
}

internal fun JSONObject.toDoc() = getString("description")

internal val suppressUnusedWarning = AnnotationSpec.builder(Suppress::class.java.asClassName())
    .addMember("\"UNUSED_PARAMETER\"")
    .build()

private val mappings = mapOf<String, KClass<*>>(
    "uuid" to UUID::class,
    "map" to Map::class,
    "string" to String::class,
    "integer" to Int::class, // TODO INTEGER and INT????
    "boolean" to Boolean::class,
    "date" to Date::class,
    "imageformat" to Object::class, // TODO
    "long" to Long::class,
    "set" to Set::class,
    "list" to List::class,
    "int" to Int::class,
    "state" to Object::class, // TODO
    "tzdate" to ZonedDateTime::class, // TODO
    "responseobject" to Object::class,
    "short" to Short::class
)
