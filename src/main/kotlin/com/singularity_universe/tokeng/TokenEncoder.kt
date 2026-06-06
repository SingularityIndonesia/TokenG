package com.singularity_universe.tokeng

import com.singularity_universe.tokeng.entity.Token

fun interface TokenEncoder {
    fun encode(token: Token): String
}
