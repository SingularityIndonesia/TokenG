package com.singularity_universe.tokeng

import com.singularity_universe.tokeng.entity.Token
import com.singularity_universe.tokeng.entity.TokenInfo
import kotlin.time.Clock

object TokenG {

/** Step 1: Wrap [com.singularity_universe.tokeng.entity.TokenInfo] into a [com.singularity_universe.tokeng.entity.Token], auto-stamping [com.singularity_universe.tokeng.entity.TokenInfo.createdAt]. */
    fun generate(tokenInfo: TokenInfo): Token {
        val stamped = tokenInfo.copy(createdAt = Clock.System.now())
        return Token(info = stamped)
    }

    /** Step 2: Attach a [signature] to the token.
     *  Signing is the caller's responsibility — provide a signature derived from
     *  your own signing mechanism (e.g. HMAC, RSA). */
    fun sign(token: Token, signature: String): Token {
        return token.copy(signature = signature)
    }

    /** Step 3: Encode the [Token] into a string using the given [encoder]. */
    fun encode(token: Token, encoder: TokenEncoder): String {
        return encoder.encode(token)
    }
}
