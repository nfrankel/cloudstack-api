package com.exoscale.api.generate

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.json.JSONObject

private const val COUNT_KEY = "count"
private const val RESPONSE_KEY = "response"

private const val LIST_COMMAND_PREFIX = "list"

private val JSONObject.hasListResult: Boolean
    get() = name.startsWith(LIST_COMMAND_PREFIX)

internal fun JSONObject.toResultSpec(): FileSpec {
    return if (hasListResult) toListResultSpec()
    else toSingleResultSpec()
}

internal fun JSONObject.toSingleResultSpec(): FileSpec {
    val constructorParameters: List<ParameterSpec> = if (has(RESPONSE_KEY)) getJSONArray(RESPONSE_KEY)
        .map { (it as JSONObject).toConstructorParameterSpec() }
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

internal fun JSONObject.toListResultSpec(): FileSpec {
    val itemName = name.substring(LIST_COMMAND_PREFIX.length, name.length - 1)
    val itemClassName = ClassName(PACKAGE_NAME, itemName)
    val resultType = TypeSpec.classBuilder(resultClassName)
        .addSuperinterface(ClassName(PACKAGE_NAME, RESULT_CLASS_NAME_SUFFIX))
        .addProperty(PropertySpec.builder("${name}response".toLowerCase(), ClassName(PACKAGE_NAME, "${name}Response"))
            .addModifiers(KModifier.LATEINIT)
            .mutable()
            .build())
        .build()
    val collectionProp = PropertySpec.Companion.builder(itemName, List::class.asClassName().parameterizedBy(itemClassName))
        .mutable()
        .addModifiers(KModifier.LATEINIT)
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

private fun JSONObject.toItemResultPropertySpec(): PropertySpec {
    val type = computePropertyType().copy()
    return PropertySpec.builder(getString(NAME_KEY), type)
        .mutable()
        .initializer("null")
        .build()
}