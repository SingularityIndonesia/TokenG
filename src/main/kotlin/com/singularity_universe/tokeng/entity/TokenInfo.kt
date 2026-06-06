package com.singularity_universe.tokeng.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * Holds all the information needed to generate a token.
 *
 * Example:
 * ```
 * val info = TokenInfo(
 *     purpose       = "payment-authorization",
 *     issuer        = "payment-service",
 *     subject       = "user-9821",
 *     acknowledgements = listOf(
 *         "gateway-service ADFFE2333FBC 1748995200000",
 *         "fraud-service   C3F1A9B72EAD 1748995200000"
 *     ),
 *     issuedAt      = Clock.System.now(),
 *     expiresAt     = Clock.System.now() + 1.hours,
 *     scope         = listOf("payment:write", "balance:read"),
 *     metadata      = mapOf("currency" to "USD", "region" to "us-east-1"),
 *     nonce         = "a3f9c1"
 * )
 *
 * val token = TokenG.generate(info, Base64JsonEncoder)
 * // createdAt is automatically stamped by TokenG.generate(), no need to set it manually.
 * ```
 *
 * @param purpose         Why the token is being issued. e.g. "authentication", "payment-authorization"
 * @param issuer          The service or entity issuing the token. e.g. "auth-service"
 * @param subject         Who the token is for. e.g. a user ID or device ID
 * @param acknowledgements List of parties acknowledging this token. Each entry is a free-form string
 *                         and can carry a signature and timestamp: "service-a <signature> <timestamp>"
 * @param issuedAt        Intentionally set by the caller — when the token request was made.
 *                         This may differ from [createdAt] if the system was delayed or jammed.
 * @param createdAt       Automated — stamped by [com.singularity_universe.tokeng.TokenG.generate] at the moment the token is produced.
 *                         Internal to the library; callers cannot set this directly.
 * @param expiresAt       When the token expires. Null means it does not expire.
 * @param scope           What actions or resources this token grants access to. e.g. ["payment:write"]
 * @param metadata        Arbitrary key-value pairs for app-specific claims. e.g. {"region" to "us-east-1"}
 * @param nonce           A unique value to prevent replay attacks. e.g. a random hex string
 */
@Serializable
data class TokenInfo(
    val purpose: String,
    val issuer: String,
    val acknowledgements: List<String>,
    val subject: String,
    @Serializable(with = InstantSerializer::class)
    val issuedAt: Instant,
    @Serializable(with = InstantSerializer::class)
    internal val createdAt: Instant? = null,
    @Serializable(with = InstantSerializer::class)
    val expiresAt: Instant? = null,
    val scope: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val nonce: String? = null
)

private object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeLong(value.toEpochMilliseconds())
    override fun deserialize(decoder: Decoder): Instant = Instant.fromEpochMilliseconds(decoder.decodeLong())
}
