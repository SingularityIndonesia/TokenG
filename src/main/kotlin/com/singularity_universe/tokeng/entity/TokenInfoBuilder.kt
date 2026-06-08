package com.singularity_universe.tokeng.entity

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Builder for [TokenInfo], used via the [tokenInfo] DSL.
 *
 * Example:
 * ```
 * val info = tokenInfo {
 *     purpose  = "authentication"
 *     issuer   = "auth-service"
 *     subject  = "user-123"
 *     expiresIn = 1.hours
 *     scope    += "profile:read"
 *     metadata["region"] = "us-east-1"
 * }
 * ```
 */
class TokenInfoBuilder {
    var purpose: String? = null
    var issuer: String? = null
    var subject: String? = null
    var issuedAt: Instant = Clock.System.now()
    var expiresAt: Instant? = null
    var expiresIn: Duration? = null
    var nonce: String? = null
    val acknowledgements: MutableList<String> = mutableListOf()
    val scope: MutableList<String> = mutableListOf()
    val metadata: MutableMap<String, String> = mutableMapOf()

    internal fun build(): TokenInfo {
        val resolvedExpiresAt = expiresIn?.let { issuedAt + it } ?: expiresAt
        return TokenInfo(
            purpose = requireNotNull(purpose) { "TokenInfo: 'purpose' is required" },
            issuer = requireNotNull(issuer) { "TokenInfo: 'issuer' is required" },
            subject = requireNotNull(subject) { "TokenInfo: 'subject' is required" },
            acknowledgements = acknowledgements.toList(),
            issuedAt = issuedAt,
            expiresAt = resolvedExpiresAt,
            scope = scope.toList(),
            metadata = metadata.toMap(),
            nonce = nonce
        )
    }
}

/**
 * DSL entry point for building a [TokenInfo].
 *
 * Required fields: [TokenInfoBuilder.purpose], [TokenInfoBuilder.issuer], [TokenInfoBuilder.subject].
 *
 * Example:
 * ```
 * val info = tokenInfo {
 *     purpose  = "payment-authorization"
 *     issuer   = "payment-service"
 *     subject  = "user-9821"
 *     expiresIn = 1.hours
 *     scope    += "payment:write"
 *     scope    += "balance:read"
 *     metadata["currency"] = "USD"
 *     acknowledgements += "fraud-service C3F1A9B72EAD 1748995200000"
 *     nonce    = "a3f9c1"
 * }
 * ```
 */
fun tokenInfo(block: TokenInfoBuilder.() -> Unit): TokenInfo =
    TokenInfoBuilder().apply(block).build()