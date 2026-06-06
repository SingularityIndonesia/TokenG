package com.singularity_universe.tokeng

import com.singularity_universe.tokeng.entity.Token

fun interface TokenDecoder {
    fun decode(encoded: String): Token
}
