package ge.tbcbank.retrocache

import retrofit2.http.Tag

typealias CacheControl = Tag

sealed interface CachePolicy {
    data object Cached : CachePolicy
    data object Refresh : CachePolicy
    data class CachedWithTime(val timeInMillis: Long) : CachePolicy
}