package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.TokenDecoder
import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Instant

/**
 * Decodes a JWT string produced by [JwtEncoder] back into a [Token].
 * Symmetric counterpart to [JwtEncoder].
 *
 * Example:
 * ```
 * val token = JwtDecoder().decode(jwt)
 * ```
 */
class JwtDecoder : TokenDecoder {

    override fun decode(encoded: String): Token {
        val parts = encoded.split(".")
        require(parts.size == 3) { "Invalid JWT: expected 3 parts, got ${parts.size}" }

        val payload = Json.parseToJsonElement(decodeBase64url(parts[1])).jsonObject
        val signature = parts[2]

        val info = TokenInfo(
            issuer          = payload["iss"]?.jsonPrimitive?.content ?: error("Missing claim: iss"),
            subject         = payload["sub"]?.jsonPrimitive?.content ?: error("Missing claim: sub"),
            issuedAt        = payload["iat"]?.jsonPrimitive?.longOrNull?.let { Instant.fromEpochSeconds(it) }
                              ?: error("Missing claim: iat"),
            expiresAt       = payload["exp"]?.jsonPrimitive?.longOrNull?.let { Instant.fromEpochSeconds(it) },
            nonce           = payload["jti"]?.jsonPrimitive?.content,
            purpose         = payload["purpose"]?.jsonPrimitive?.content ?: error("Missing claim: purpose"),
            acknowledgements = payload["acknowledgements"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
            scope           = payload["scope"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
            metadata        = payload["metadata"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap(),
            createdAt       = payload["created_at"]?.jsonPrimitive?.longOrNull?.let { Instant.fromEpochSeconds(it) }
        )

        return Token(info = info, signature = signature)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeBase64url(value: String): String {
        val padded = value.padEnd(value.length + (4 - value.length % 4) % 4, '=')
        return Base64.UrlSafe.decode(padded).decodeToString()
    }
}
