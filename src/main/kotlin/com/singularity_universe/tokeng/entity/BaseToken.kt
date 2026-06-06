package com.singularity_universe.tokeng.entity

/**
 * Common base for all token states.
 * A token begins as [UnsignedToken] and becomes a [Token] after signing.
 */
sealed class BaseToken {
    abstract val info: TokenInfo
}
