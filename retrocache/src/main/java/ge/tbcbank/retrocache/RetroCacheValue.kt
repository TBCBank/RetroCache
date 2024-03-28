package ge.tbcbank.retrocache

import okhttp3.Protocol
import java.io.Serializable

data class RetroCacheValue(
    val responseJson: String?,
    val responseCode: Int,
    val responseProtocol: Protocol,
    val responseHeaders: Array<String>,
    val tag: String? = null,
    val expirationTime: Long? = null,
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RetroCacheValue

        if (responseJson != other.responseJson) return false
        if (responseCode != other.responseCode) return false
        if (responseProtocol != other.responseProtocol) return false
        if (!responseHeaders.contentEquals(other.responseHeaders)) return false
        if (tag != other.tag) return false
        return expirationTime == other.expirationTime
    }

    override fun hashCode(): Int {
        var result = responseJson?.hashCode() ?: 0
        result = 31 * result + responseCode
        result = 31 * result + responseProtocol.hashCode()
        result = 31 * result + responseHeaders.contentHashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + (expirationTime?.hashCode() ?: 0)
        return result
    }


}
