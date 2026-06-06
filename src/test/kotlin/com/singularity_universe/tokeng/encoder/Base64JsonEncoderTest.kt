package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.TokenG
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class Base64JsonEncoderTest {

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

    private val token: Token = TokenG.sign(TokenG.generate(info), "test-signature")

    @Test
    fun `encode returns non-blank string`() {
        val result = Base64JsonEncoder.encode(token)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `encode returns valid base64url (no padding, no illegal characters)`() {
        val result = Base64JsonEncoder.encode(token)
        assertFalse(result.contains('='))
        assertFalse(result.contains('+'))
        assertFalse(result.contains('/'))
    }

    @Test
    fun `encoded output decodes back to valid json`() {
        val result = Base64JsonEncoder.encode(token)
        val decoded = result.decodeBase64url()
        val json = Json.parseToJsonElement(decoded)
        assertTrue(json.jsonObject.isNotEmpty())
    }

    @Test
    fun `decoded json contains expected fields`() {
        val result = Base64JsonEncoder.encode(token)
        val json = Json.parseToJsonElement(result.decodeBase64url()).jsonObject
        assertEquals("authentication", json["purpose"]?.jsonPrimitive?.content)
        assertEquals("test-issuer", json["issuer"]?.jsonPrimitive?.content)
        assertEquals("user-123", json["subject"]?.jsonPrimitive?.content)
    }

    @Test
    fun `decoded json contains signature`() {
        val result = Base64JsonEncoder.encode(token)
        val json = Json.parseToJsonElement(result.decodeBase64url()).jsonObject
        assertEquals("test-signature", json["signature"]?.jsonPrimitive?.content)
    }

    @Test
    fun `different token info produces different output`() {
        val info2 = info.copy(subject = "user-999")
        val token2 = TokenG.sign(TokenG.generate(info2), "test-signature")
        val result1 = Base64JsonEncoder.encode(token)
        val result2 = Base64JsonEncoder.encode(token2)
        assertTrue(result1 != result2)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun String.decodeBase64url(): String =
        Base64.UrlSafe.decode(this.padEnd(this.length + (4 - this.length % 4) % 4, '=')).decodeToString()
}
