package com.singularity_universe.tokeng.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class TokenInfoBuilderTest {

    @Test
    fun `tokenInfo builds with required fields`() {
        val info = tokenInfo {
            purpose = "authentication"
            issuer = "auth-service"
            subject = "user-123"
        }
        assertEquals("authentication", info.purpose)
        assertEquals("auth-service", info.issuer)
        assertEquals("user-123", info.subject)
    }

    @Test
    fun `tokenInfo expiresIn resolves to expiresAt`() {
        val info = tokenInfo {
            purpose = "auth"
            issuer = "svc"
            subject = "user-1"
            expiresIn = 1.hours
        }
        assertNotNull(info.expiresAt)
        assertEquals(info.issuedAt + 1.hours, info.expiresAt)
    }

    @Test
    fun `tokenInfo expiresIn takes precedence over expiresAt`() {
        val explicit = Clock.System.now() + 2.hours
        val info = tokenInfo {
            purpose = "auth"
            issuer = "svc"
            subject = "user-1"
            expiresAt = explicit
            expiresIn = 1.hours
        }
        assertEquals(info.issuedAt + 1.hours, info.expiresAt)
    }

    @Test
    fun `tokenInfo with no expiry has null expiresAt`() {
        val info = tokenInfo {
            purpose = "auth"
            issuer = "svc"
            subject = "user-1"
        }
        assertNull(info.expiresAt)
    }

    @Test
    fun `tokenInfo scope and metadata and acknowledgements are mutable in block`() {
        val info = tokenInfo {
            purpose = "payment"
            issuer = "payment-svc"
            subject = "user-9821"
            scope += "payment:write"
            scope += "balance:read"
            metadata["currency"] = "USD"
            acknowledgements += "fraud-service C3F1A9B72EAD 1748995200000"
        }
        assertEquals(listOf("payment:write", "balance:read"), info.scope)
        assertEquals(mapOf("currency" to "USD"), info.metadata)
        assertEquals(listOf("fraud-service C3F1A9B72EAD 1748995200000"), info.acknowledgements)
    }

    @Test
    fun `tokenInfo throws when purpose is missing`() {
        assertFailsWith<IllegalArgumentException> {
            tokenInfo {
                issuer = "svc"
                subject = "user-1"
            }
        }
    }

    @Test
    fun `tokenInfo throws when issuer is missing`() {
        assertFailsWith<IllegalArgumentException> {
            tokenInfo {
                purpose = "auth"
                subject = "user-1"
            }
        }
    }

    @Test
    fun `tokenInfo throws when subject is missing`() {
        assertFailsWith<IllegalArgumentException> {
            tokenInfo {
                purpose = "auth"
                issuer = "svc"
            }
        }
    }
}
