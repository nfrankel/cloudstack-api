package com.exoscale.api.generate

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.json.JSONObject

private const val PARAMS_KEY = "params"

private const val COMMAND_ATTR_NAME = "command"

private const val COMMAND_CLASS_NAME_SUFFIX = "Command"

private const val RESULT_TYPE_ATTR_NAME = "resultType"

internal fun JSONObject.toCommandSpec(): FileSpec {
    val constructorParameters: List<ParameterSpec> = if (has(PARAMS_KEY)) {
        getJSONArray(PARAMS_KEY)
            .map { it as JSONObject }
            .sortedByDescending { it.isRequired }
            .map { it.toConstructorParameterSpec() }
    } else arrayListOf()
    val constructor = FunSpec.constructorBuilder()
        .addParameters(constructorParameters)
        .build()
    val resultType = ClassName(PACKAGE_NAME, resultClassName)
    val commandSupertype = ClassName(PACKAGE_NAME, COMMAND_CLASS_NAME_SUFFIX).parameterizedBy(resultType)
    val commandIdProperty = PropertySpec.builder(COMMAND_ATTR_NAME, String::class.asTypeName())
        .addModifiers(KModifier.OVERRIDE)
        .initializer("\"$name\"")
        .build()
    val resultTypeProperty = PropertySpec.builder(RESULT_TYPE_ATTR_NAME, Class::class.asTypeName().parameterizedBy(resultType))
        .addModifiers(KModifier.OVERRIDE)
        .initializer("${resultType.canonicalName}::class.java")
        .build()
    val properties: List<PropertySpec> = if (has(PARAMS_KEY)) {
        getJSONArray(PARAMS_KEY)
            .map { it as JSONObject }
            .map { it.toCommandPropertySpec() }
    } else arrayListOf()
    val commandType = TypeSpec.classBuilder(name.capitalize())
        .addSuperinterface(commandSupertype)
        .primaryConstructor(constructor)
        .addKdoc("${toDoc()}.${System.lineSeparator()}${System.lineSeparator()}")
        .addProperty(commandIdProperty)
        .addProperty(resultTypeProperty)
        .addProperties(properties)
        .build()
    return FileSpec.builder(PACKAGE_NAME, name.capitalize())
        .addType(commandType)
        .build()
}

private fun JSONObject.toCommandPropertySpec(): PropertySpec {
    val type = computeParameterType()
    return PropertySpec.builder(getString(NAME_KEY), type)
        .mutable(true)
        .initializer(name)
        .build()
}