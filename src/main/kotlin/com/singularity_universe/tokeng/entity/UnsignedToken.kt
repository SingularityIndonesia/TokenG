package com.singularity_universe.tokeng.entity

/**
 * Represents a token that has been generated but not yet signed.
 * Must be passed to [com.singularity_universe.tokeng.TokenG.sign] to produce a [Token].
 */
data class UnsignedToken(
    override val info: TokenInfo
) : BaseToken()
