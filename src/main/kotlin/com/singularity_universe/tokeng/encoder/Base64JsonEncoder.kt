package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.TokenEncoder
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object Base64JsonEncoder : TokenEncoder {
    private val json = Json { encodeDefaults = true }

    @OptIn(ExperimentalEncodingApi::class)
    override fun encode(token: Token): String {
        val jsonString = json.encodeToString(TokenInfo.serializer(), token.info)
        return Base64.UrlSafe.encode(jsonString.toByteArray()).trimEnd('=')
    }
}
