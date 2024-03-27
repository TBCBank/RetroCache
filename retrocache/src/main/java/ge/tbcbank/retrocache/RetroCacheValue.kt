package ge.tbcbank.retrocache

import java.io.Serializable

data class RetroCacheValue(
    val responseJson: String?,
    val tag: String? = null,
    val expirationTime: Long? = null,
) : Serializable
