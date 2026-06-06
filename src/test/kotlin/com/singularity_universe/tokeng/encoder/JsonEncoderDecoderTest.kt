package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.TokenG
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class JsonEncoderDecoderTest {

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

    private val token = TokenG.sign(TokenG.generate(info), "test-signature")

    // --- Encoder ---

    @Test
    fun `encode returns valid json string`() {
        val result = JsonEncoder.encode(token)
        val json = Json.parseToJsonElement(result)
        assertTrue(json.jsonObject.isNotEmpty())
    }

    @Test
    fun `encode output is not base64`() {
        val result = JsonEncoder.encode(token)
        assertFalse(result.startsWith("{").not()) // starts with '{'
    }

    @Test
    fun `encode includes all expected fields`() {
        val result = Json.parseToJsonElement(JsonEncoder.encode(token)).jsonObject
        assertEquals("authentication", result["purpose"]?.jsonPrimitive?.content)
        assertEquals("test-issuer", result["issuer"]?.jsonPrimitive?.content)
        assertEquals("user-123", result["subject"]?.jsonPrimitive?.content)
        assertEquals("test-signature", result["signature"]?.jsonPrimitive?.content)
    }

    // --- Decoder ---

    @Test
    fun `decode returns token with correct signature`() {
        val decoded = JsonDecoder.decode(JsonEncoder.encode(token))
        assertEquals("test-signature", decoded.signature)
    }

    @Test
    fun `decode returns token with correct info fields`() {
        val decoded = JsonDecoder.decode(JsonEncoder.encode(token))
        assertEquals(info.purpose, decoded.info.purpose)
        assertEquals(info.issuer, decoded.info.issuer)
        assertEquals(info.subject, decoded.info.subject)
        assertEquals(info.nonce, decoded.info.nonce)
        assertEquals(info.scope, decoded.info.scope)
        assertEquals(info.metadata, decoded.info.metadata)
        assertEquals(info.acknowledgements, decoded.info.acknowledgements)
    }

    // --- Roundtrip ---

    @Test
    fun `encode then decode roundtrip produces equal token`() {
        val decoded = JsonDecoder.decode(JsonEncoder.encode(token))
        assertEquals(token.signature, decoded.signature)
        assertEquals(token.info.purpose, decoded.info.purpose)
        assertEquals(token.info.issuer, decoded.info.issuer)
        assertEquals(token.info.subject, decoded.info.subject)
    }
}
