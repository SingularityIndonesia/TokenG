package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.TokenEncoder
import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

object JsonEncoder : TokenEncoder {
    private val json = Json { encodeDefaults = true }

    override fun encode(token: Token): String {
        val infoFields = json.encodeToJsonElement(TokenInfo.serializer(), token.info).jsonObject
        return buildJsonObject {
            infoFields.forEach { (k, v) -> put(k, v) }
            put("signature", token.signature)
        }.toString()
    }
}
