package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.TokenG
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class JwtDecoderTest {

    private val info = TokenInfo(
        purpose = "authentication",
        issuer = "test-issuer",
        acknowledgements = listOf(
            "service-a ADFFE2333FBC 1748995200000",
            "service-b C3F1A9B72EAD 1748995200000"
        ),
        subject = "user-123",
        issuedAt = Clock.System.now(),
        expiresAt = Clock.System.now().plus(1.hours),
        scope = listOf("payment:write", "balance:read"),
        metadata = mapOf("region" to "us-east-1"),
        nonce = "a3f9c1"
    )

    private val encoder = JwtEncoder()
    private val unsigned = TokenG.generate(info)
    private val token = TokenG.sign(unsigned, "mock-hmac-signature")
    private val jwt = encoder.encode(token)

    @Test
    fun `decode returns token with correct signature`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(token.signature, decoded.signature)
    }

    @Test
    fun `decode returns token with correct issuer`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(info.issuer, decoded.info.issuer)
    }

    @Test
    fun `decode returns token with correct subject`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(info.subject, decoded.info.subject)
    }

    @Test
    fun `decode returns token with correct purpose`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(info.purpose, decoded.info.purpose)
    }

    @Test
    fun `decode returns token with correct issuedAt`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(info.issuedAt.epochSeconds, decoded.info.issuedAt.epochSeconds)
    }

    @Test
    fun `decode returns token with correct expiresAt`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(info.expiresAt?.epochSeconds, decoded.info.expiresAt?.epochSeconds)
    }

    @Test
    fun `decode returns token with correct nonce`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(info.nonce, decoded.info.nonce)
    }

    @Test
    fun `decode returns token with correct acknowledgements`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(info.acknowledgements, decoded.info.acknowledgements)
    }

    @Test
    fun `decode returns token with correct scope`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(info.scope, decoded.info.scope)
    }

    @Test
    fun `decode returns token with correct metadata`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(info.metadata, decoded.info.metadata)
    }

    @Test
    fun `encode then decode roundtrip produces equal token`() {
        val decoded = JwtDecoder().decode(jwt)
        assertEquals(token.signature, decoded.signature)
        assertEquals(token.info.issuer, decoded.info.issuer)
        assertEquals(token.info.subject, decoded.info.subject)
        assertEquals(token.info.issuedAt.epochSeconds, decoded.info.issuedAt.epochSeconds)
    }
}
