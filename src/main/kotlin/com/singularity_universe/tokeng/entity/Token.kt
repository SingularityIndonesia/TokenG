package com.singularity_universe.tokeng.entity

/**
 * Represents a signed token, ready to be encoded.
 *
 * @param info      The metadata describing the token's purpose, issuer, subject, etc.
 * @param signature The unique value that authenticates this token. It is the caller's
 *                  responsibility to ensure the signature is unique per issuance — consider
 *                  deriving it from [TokenInfo.nonce], [TokenInfo.issuedAt], or a cryptographic
 *                  function to prevent collision across token instances.
 */
data class Token(
    override val info: TokenInfo,
    val signature: String
) : BaseToken()
