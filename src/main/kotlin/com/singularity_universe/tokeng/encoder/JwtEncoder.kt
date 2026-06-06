package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.TokenEncoder
import com.singularity_universe.tokeng.entity.BaseToken
import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.entity.UnsignedToken
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Encodes a [Token] into a JWT string: `base64url(header).base64url(payload).signature`
 *
 * The signature must be computed over the signing input, which is `base64url(header).base64url(payload)`.
 * Use [signingInput] to get that string, sign it with your own mechanism, then pass the result
 * to [com.singularity_universe.tokeng.TokenG.sign] before encoding.
 *
 * Standard JWT claims are mapped from [com.singularity_universe.tokeng.entity.TokenInfo]:
 * - `iss` → issuer
 * - `sub` → subject
 * - `iat` → issuedAt (epoch seconds)
 * - `exp` → expiresAt (epoch seconds, omitted if null)
 * - `jti` → nonce (omitted if null)
 *
 * Non-standard claims: `purpose`, `acknowledgements`, `scope`, `metadata`, `created_at`.
 *
 * @param algorithm The algorithm declared in the JWT header. Defaults to `"HS256"`.
 *                  Must match the algorithm used by the caller when producing the signature.
 *
 * Example:
 * ```
 * val encoder      = JwtEncoder(algorithm = "HS256")
 * val unsigned     = TokenG.generate(info)
 * val signingInput = encoder.signingInput(unsigned)   // base64url(header).base64url(payload)
 * val signed       = TokenG.sign(unsigned, hmac(signingInput, secretKey))
 * val jwt          = TokenG.encode(signed, encoder)
 * ```
 */
class JwtEncoder(private val algorithm: String = "HS256") : TokenEncoder {

    /**
     * Returns the string the caller must sign: `base64url(header).base64url(payload)`.
     * Pass the result of signing this to [com.singularity_universe.tokeng.TokenG.sign].
     */
    fun signingInput(token: UnsignedToken): String {
        val headerEncoded  = base64url(header().toString())
        val payloadEncoded = base64url(payload(token).toString())
        return "$headerEncoded.$payloadEncoded"
    }

    override fun encode(token: Token): String {
        val headerEncoded  = base64url(header().toString())
        val payloadEncoded = base64url(payload(token).toString())
        return "$headerEncoded.$payloadEncoded.${token.signature}"
    }

    private fun header() = buildJsonObject {
        put("alg", algorithm)
        put("typ", "JWT")
    }

    private fun payload(token: BaseToken) = buildJsonObject {
        put("iss", token.info.issuer)
        put("sub", token.info.subject)
        put("iat", token.info.issuedAt.epochSeconds)
        token.info.expiresAt?.let { put("exp", it.epochSeconds) }
        token.info.nonce?.let { put("jti", it) }
        put("purpose", token.info.purpose)
        putJsonArray("acknowledgements") {
            token.info.acknowledgements.forEach { add(JsonPrimitive(it)) }
        }
        if (token.info.scope.isNotEmpty()) {
            putJsonArray("scope") { token.info.scope.forEach { add(JsonPrimitive(it)) } }
        }
        if (token.info.metadata.isNotEmpty()) {
            putJsonObject("metadata") { token.info.metadata.forEach { (k, v) -> put(k, JsonPrimitive(v)) } }
        }
        token.info.createdAt?.let { put("created_at", it.epochSeconds) }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun base64url(value: String): String =
        Base64.UrlSafe.encode(value.toByteArray()).trimEnd('=')
}
