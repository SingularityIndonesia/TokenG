package com.singularity_universe.tokeng

import com.singularity_universe.tokeng.encoder.Base64JsonEncoder
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class TokenGTest {

    private val info = TokenInfo(
        purpose = "authentication",
        issuer = "test-issuer",
        acknowledgements = listOf(
            "service-a ADFFE2333FBC 1748995200000",
            "service-b C3F1A9B72EAD 1748995200000"
        ),
        subject = "user-123",
        issuedAt = Clock.System.now()
    )

    @Test
    fun `generate wraps info into token with UNSIGNED signature`() {
        val token = TokenG.generate(info)
        assertEquals("UNSIGNED", token.signature)
    }

    @Test
    fun `generate stamps createdAt automatically`() {
        val token = TokenG.generate(info)
        assertTrue(token.info.createdAt != null)
    }

    @Test
    fun `sign attaches the provided signature`() {
        val token = TokenG.generate(info)
        val signed = TokenG.sign(token, "ADFFE2333FBC")
        assertEquals("ADFFE2333FBC", signed.signature)
    }

    @Test
    fun `encode returns non-blank string`() {
        val token = TokenG.generate(info)
        val signed = TokenG.sign(token, "ADFFE2333FBC")
        val result = TokenG.encode(signed, Base64JsonEncoder)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `encode returns different results for different issuedAt`() {
        val info2 = info.copy(issuedAt = info.issuedAt.plus(1.seconds))
        val result1 = TokenG.encode(TokenG.sign(TokenG.generate(info), "SIG"), Base64JsonEncoder)
        val result2 = TokenG.encode(TokenG.sign(TokenG.generate(info2), "SIG"), Base64JsonEncoder)
        assertNotEquals(result1, result2)
    }

    @Test
    fun `encode with custom encoder returns encoder output`() {
        val token = TokenG.generate(info)
        val signed = TokenG.sign(token, "ADFFE2333FBC")
        val result = TokenG.encode(signed, encoder = { "custom-token" })
        assertEquals("custom-token", result)
    }
}
