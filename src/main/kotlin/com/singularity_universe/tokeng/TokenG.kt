package com.singularity_universe.tokeng

import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.entity.TokenInfo
import com.singularity_universe.tokeng.entity.UnsignedToken
import kotlin.time.Clock

object TokenG {

    /** Step 1: Wrap [TokenInfo] into an [UnsignedToken], auto-stamping [TokenInfo.createdAt]. */
    fun generate(tokenInfo: TokenInfo): UnsignedToken {
        val stamped = tokenInfo.copy(createdAt = Clock.System.now())
        return UnsignedToken(info = stamped)
    }

    /** Step 2: Attach a [signature] to an [UnsignedToken], producing a signed [Token].
     *  Signing is the caller's responsibility — provide a signature derived from
     *  your own signing mechanism (e.g. HMAC, RSA). */
    fun sign(token: UnsignedToken, signature: String): Token {
        return Token(info = token.info, signature = signature)
    }

    /** Step 3: Encode the signed [Token] into a string using the given [encoder]. */
    fun encode(token: Token, encoder: TokenEncoder): String {
        return encoder.encode(token)
    }
}
