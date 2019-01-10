package com.exoscale.api

import com.exoscale.api.Output.JSON
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpGet
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.full.declaredMemberProperties
import kotlin.text.Charsets.UTF_8

typealias Execute = ExoscaleClient

class ExoscaleClient internal constructor(internal val baseUrl: String,
                                          internal val apiKey: String,
                                          internal val apiSecret: String) {

    private val HMAC_SHA1 = "HmacSHA1"

    private val hmac = Mac.getInstance(HMAC_SHA1).apply {
        init(SecretKeySpec(apiSecret.encode(), HMAC_SHA1))
    }

    @JvmOverloads operator fun <R : Result> invoke(command: Command<R>, output: Output = JSON): R {
        val parametersMap = mapOf("response" to output.type, "apikey" to apiKey)
            .plus(command::class.declaredMemberProperties
                .filter { it.name != "resultType" }
                .filter { it.getter.call(command) != null }
                .filter {
                    val att = it.getter.call(command)
                    !(att is Map<*,*> || att is Collection<*> ) // TODO Check how to handle multi-valued parameters
                }
                .associateBy(
                    { it.name },
                    { it.getter.call(command) as String }
                ))
        val response = baseUrl.httpGet(parametersMap.toListWithSignature())
            .responseObject(object : ResponseDeserializable<R> {
                override fun deserialize(content: String) =
                    ObjectMapper().apply {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    }.readValue<R>(content, command.resultType)
            })
        val result = response.third
        result.fold(
            { return it },
            { throw it })
    }

    private fun Map<String, String>.queryString(): String {
        return entries
            .sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value}" }
            .toLowerCase()
    }

    private fun Map<String, String>.toListWithSignature(): List<Pair<String, String>> {
        val queryString = queryString()
        val digest = hmac.doFinal(queryString.encode())
        val signature = Base64.getEncoder().encode(digest).decode()
        return this.plus("signature" to signature).toList()
    }

    private fun String.encode() = toByteArray(UTF_8)
    private fun ByteArray.decode() = toString(UTF_8)
}
