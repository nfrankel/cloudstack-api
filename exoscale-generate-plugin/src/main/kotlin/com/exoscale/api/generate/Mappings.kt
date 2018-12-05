package com.exoscale.api.generate

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.json.JSONObject
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass

const val PACKAGE_NAME = "com.exoscale.api"

fun JSONObject.toSpec(): FileSpec {
    val name = getString("name")
    val className = name.capitalize()
    val constructorParameters: List<ParameterSpec> = if (has("params")) getJSONArray("params")
        .sortedByDescending { (it as JSONObject).getBoolean("required") }
        .map { (it as JSONObject).toParameterSpec() }
    else arrayListOf()
    val constructor = FunSpec.constructorBuilder()
        .addParameters(constructorParameters)
        .build()
    val commandSupertype = ClassName(PACKAGE_NAME, "Command")
    val commandIdProperty = PropertySpec.builder("commandId", String::class.asTypeName())
        .addModifiers(KModifier.OVERRIDE)
        .initializer("\"$name\"")
        .build()
    val commandType = TypeSpec.classBuilder(className)
        .addSuperinterface(commandSupertype)
        .primaryConstructor(constructor)
        .addProperty(commandIdProperty)
        .build()
    return FileSpec.builder(PACKAGE_NAME, className)
        .addType(commandType)
        .build()
}

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

private fun JSONObject.toParameterSpec(): ParameterSpec {
    val required = getBoolean("required")
    val type = computeType(required)
    return ParameterSpec.builder(getString("name"), type).apply {
        if (type is ParameterizedTypeName) {
            when {
                type.rawType == List::class.asTypeName() -> defaultValue("arrayListOf()")
                type.rawType == Set::class.asTypeName() -> defaultValue("setOf()")
                type.rawType == Map::class.asTypeName() -> defaultValue("mapOf()")
            }
        } else if (type.isNullable) defaultValue("null")
    }.build()
}

private fun JSONObject.computeType(required: Boolean): TypeName {
    val base: KClass<out Any> = mappings[getString("type")] ?: Nothing::class
    return when (base) {
        List::class, Set::class -> base.parameterizedBy(String::class)
        Map::class -> base.parameterizedBy(String::class, String::class)
        else -> {
            if (required) base.asTypeName()
            else base.asTypeName().copy(true)
        }
    }
}