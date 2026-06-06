package com.singularity_universe.tokeng.encoder

import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.TokenG
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class JwtEncoderTest {

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
    fun `jwt has three parts separated by dots`() {
        val jwt = JwtEncoder().encode(token)
        assertEquals(3, jwt.split(".").size)
    }

    @Test
    fun `header declares correct algorithm and type`() {
        val jwt = JwtEncoder(algorithm = "HS256").encode(token)
        val header = jwt.split(".")[0].decodeBase64url()
        assertTrue(header.contains("\"alg\":\"HS256\""))
        assertTrue(header.contains("\"typ\":\"JWT\""))
    }

    @Test
    fun `payload contains standard claims`() {
        val jwt = JwtEncoder().encode(token)
        val payload = jwt.split(".")[1].decodeBase64url()
        assertTrue(payload.contains("\"iss\":\"test-issuer\""))
        assertTrue(payload.contains("\"sub\":\"user-123\""))
        assertTrue(payload.contains("\"iat\":"))
        assertTrue(payload.contains("\"exp\":"))
        assertTrue(payload.contains("\"jti\":\"a3f9c1\""))
    }

    @Test
    fun `payload contains non-standard claims`() {
        val jwt = JwtEncoder().encode(token)
        val payload = jwt.split(".")[1].decodeBase64url()
        assertTrue(payload.contains("\"purpose\":\"authentication\""))
        assertTrue(payload.contains("\"acknowledgements\""))
        assertTrue(payload.contains("\"scope\""))
        assertTrue(payload.contains("\"metadata\""))
    }

    @Test
    fun `signature part matches token signature`() {
        val jwt = JwtEncoder().encode(token)
        val signature = jwt.split(".")[2]
        assertEquals("test-signature", signature)
    }

    @Test
    fun `default algorithm is HS256`() {
        val jwt = JwtEncoder().encode(token)
        val header = jwt.split(".")[0].decodeBase64url()
        assertTrue(header.contains("\"alg\":\"HS256\""))
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun String.decodeBase64url(): String =
        Base64.UrlSafe.decode(this.padEnd(this.length + (4 - this.length % 4) % 4, '=')).decodeToString()
}
