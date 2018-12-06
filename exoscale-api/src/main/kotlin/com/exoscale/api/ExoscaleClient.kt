package com.exoscale.api

import com.exoscale.api.Output.JSON
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.util.*
import javax.crypto.*
import javax.crypto.spec.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.text.Charsets.UTF_8

class ExoscaleClient internal constructor (internal val baseUrl: String,
                                           internal val apiKey: String,
                                           internal val apiSecret: String) {

    private val HMAC_SHA1 = "HmacSHA1"

    private val hmac = Mac.getInstance(HMAC_SHA1).apply {
        init(SecretKeySpec(apiSecret.encode(), HMAC_SHA1))
    }

    operator fun invoke(command: Command, output: Output = JSON): Triple<Request, Response, Result<String, FuelError>> {
        val parametersMap = mapOf("command" to command.commandId, "response" to output.type, "apikey" to apiKey)
                .plus(command::class.declaredMemberProperties.associateBy(
                        { it.name },
                        { it.getter.call(command) as String }
                ))
        return baseUrl.httpGet(parametersMap.toListWithSignature()).responseString()
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
