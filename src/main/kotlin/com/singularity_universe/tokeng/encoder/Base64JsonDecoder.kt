package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.TokenDecoder
import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object Base64JsonDecoder : TokenDecoder {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @OptIn(ExperimentalEncodingApi::class)
    override fun decode(encoded: String): Token {
        val padded = encoded.padEnd(encoded.length + (4 - encoded.length % 4) % 4, '=')
        val jsonString = Base64.UrlSafe.decode(padded).decodeToString()
        val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
        val signature = jsonObject["signature"]?.jsonPrimitive?.content
            ?: error("Missing signature field in encoded token")
        val info = json.decodeFromJsonElement<TokenInfo>(JsonObject(jsonObject - "signature"))
        return Token(info = info, signature = signature)
    }
}
