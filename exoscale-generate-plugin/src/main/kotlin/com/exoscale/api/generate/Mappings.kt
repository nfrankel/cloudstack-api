package com.exoscale.api.generate

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.LATEINIT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.json.JSONObject
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass

private const val PACKAGE_NAME = "com.exoscale.api"

private const val NAME_KEY = "name"
private const val PARAMS_KEY = "params"
private const val COUNT_KEY = "count"
private const val REQUIRED_KEY = "required"
private const val RESPONSE_KEY = "response"

private const val COMMAND_ATTR_NAME = "command"
private const val RESULT_TYPE_ATTR_NAME = "resultType"

private const val LIST_COMMAND_PREFIX = "list"
private const val COMMAND_CLASS_NAME_SUFFIX = "Command"
private const val RESULT_CLASS_NAME_SUFFIX = "Result"

fun JSONObject.toCommandSpec(): FileSpec {
    val constructorParameters: List<ParameterSpec> = if (has(PARAMS_KEY)) {
        getJSONArray(PARAMS_KEY)
            .map { it as JSONObject }
            .sortedByDescending { it.isRequired }
            .map { it.toCommandConstructorParameterSpec() }
    } else arrayListOf()
    val constructor = FunSpec.constructorBuilder()
        .addParameters(constructorParameters)
        .build()
    val resultType = ClassName(PACKAGE_NAME, resultClassName)
    val commandSupertype = ClassName(PACKAGE_NAME, COMMAND_CLASS_NAME_SUFFIX).parameterizedBy(resultType)
    val commandIdProperty = PropertySpec.builder(COMMAND_ATTR_NAME, String::class.asTypeName())
        .addModifiers(OVERRIDE)
        .initializer("\"$name\"")
        .build()
    val resultTypeProperty = PropertySpec.builder(RESULT_TYPE_ATTR_NAME, Class::class.asTypeName().parameterizedBy(resultType))
        .addModifiers(OVERRIDE)
        .initializer("${resultType.canonicalName}::class.java")
        .build()
    val properties: List<PropertySpec> = if (has(PARAMS_KEY)) {
        getJSONArray(PARAMS_KEY)
            .map { it as JSONObject }
            .filter { !it.isRequired }
            .map { it.toCommandPropertySpec() }
    } else arrayListOf()
    val commandType = TypeSpec.classBuilder(name.capitalize())
        .addSuperinterface(commandSupertype)
        .primaryConstructor(constructor)
        .addProperty(commandIdProperty)
        .addProperty(resultTypeProperty)
        .addProperties(properties)
        .build()
    return FileSpec.builder(PACKAGE_NAME, name.capitalize())
        .addType(commandType)
        .build()
}

fun JSONObject.toResultSpec(): FileSpec {
    return if (hasListResult) toListResultSpec()
    else toSingleResultSpec()
}

fun JSONObject.toListResultSpec(): FileSpec {
    val itemName = name.substring(LIST_COMMAND_PREFIX.length, name.length - 1)
    val itemClassName = ClassName(PACKAGE_NAME, itemName)
    val resultType = TypeSpec.classBuilder(resultClassName)
        .addSuperinterface(ClassName(PACKAGE_NAME, RESULT_CLASS_NAME_SUFFIX))
        .addProperty(PropertySpec
            .builder("${name}response".toLowerCase(), ClassName(PACKAGE_NAME, "${name}Response"))
            .addModifiers(LATEINIT)
            .mutable()
            .build())
        .build()
    val collectionProp = PropertySpec.builder(itemName, List::class.asClassName().parameterizedBy(itemClassName))
        .mutable()
        .addModifiers(LATEINIT)
        .build()
    val properties: List<PropertySpec> = if (has(RESPONSE_KEY)) getJSONArray(RESPONSE_KEY)
        .map { (it as JSONObject).toItemResultPropertySpec() }
    else arrayListOf()
    val listResponseType = TypeSpec.classBuilder(ClassName(PACKAGE_NAME, "${name}Response"))
        .addProperty(PropertySpec.builder(COUNT_KEY, Int::class).mutable().initializer("0").build())
        .addProperty(collectionProp)
        .build()
    val itemResultType = TypeSpec.classBuilder(itemClassName)
        .addProperties(properties)
        .build()
    return FileSpec.builder(PACKAGE_NAME, resultClassName)
        .addType(resultType)
        .addType(listResponseType)
        .addType(itemResultType)
        .build()
}

fun JSONObject.toSingleResultSpec(): FileSpec {
    val constructorParameters: List<ParameterSpec> = if (has(RESPONSE_KEY)) getJSONArray(RESPONSE_KEY)
        .map { (it as JSONObject).toCommandConstructorParameterSpec() }
    else arrayListOf()
    val constructor = FunSpec.constructorBuilder()
        .addParameters(constructorParameters)
        .build()
    val resultType = TypeSpec.classBuilder(resultClassName)
        .addSuperinterface(ClassName(PACKAGE_NAME, "Result"))
        .primaryConstructor(constructor)
        .build()
    return FileSpec.builder(PACKAGE_NAME, resultClassName)
        .addType(resultType)
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

private val JSONObject.name
    get() = getString(NAME_KEY)

private val JSONObject.resultClassName
    get() = "${name.capitalize()}$RESULT_CLASS_NAME_SUFFIX"

private val JSONObject.isRequired: Boolean
    get() {
        return if (has(REQUIRED_KEY)) getBoolean(REQUIRED_KEY)
        else true
    }

private val JSONObject.hasListResult: Boolean
    get() = name.startsWith(LIST_COMMAND_PREFIX)

private fun JSONObject.toCommandConstructorParameterSpec(): ParameterSpec {
    val type = computeParameterType()
    return ParameterSpec.builder(getString(NAME_KEY), type).apply {
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

private fun JSONObject.toCommandPropertySpec(): PropertySpec {
    val type = computeParameterType()
    return PropertySpec.builder(getString(NAME_KEY), type).mutable(true).apply {
        if (type is ParameterizedTypeName) {
            when {
                type.rawType == List::class.asTypeName() -> initializer("arrayListOf()")
                type.rawType == Set::class.asTypeName() -> initializer("setOf()")
                type.rawType == Map::class.asTypeName() -> initializer("mapOf()")
            }
        } else if (type.isNullable) {
            initializer("null")
        }
    }.build()
}

private fun JSONObject.toItemResultPropertySpec(): PropertySpec {
    val type = computePropertyType().copy()
    return PropertySpec.builder(getString(NAME_KEY), type)
        .mutable()
        .initializer("null")
        .build()
}

private fun JSONObject.computePropertyType(): TypeName {
    val base: KClass<out Any> = mappings[getString("type")] ?: Nothing::class
    return when (base) {
        List::class, Set::class -> base.parameterizedBy(Any::class)
        Map::class -> base.parameterizedBy(String::class, Any::class)
        else -> base.asTypeName()
    }.copy(true)
}

private fun JSONObject.computeParameterType(): TypeName {
    val base: KClass<out Any> = mappings[getString("type")] ?: Nothing::class
    return when (base) {
        List::class, Set::class -> base.parameterizedBy(String::class)
        Map::class -> base.parameterizedBy(String::class, String::class)
        else -> base.asTypeName().copy(!isRequired)
    }
}