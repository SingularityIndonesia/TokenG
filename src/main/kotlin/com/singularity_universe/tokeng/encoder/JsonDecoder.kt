package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.TokenDecoder
import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object JsonDecoder : TokenDecoder {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun decode(encoded: String): Token {
        val jsonObject = Json.parseToJsonElement(encoded).jsonObject
        val signature = jsonObject["signature"]?.jsonPrimitive?.content
            ?: error("Missing signature field in encoded token")
        val info = json.decodeFromJsonElement<TokenInfo>(JsonObject(jsonObject - "signature"))
        return Token(info = info, signature = signature)
    }
}
