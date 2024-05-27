package ge.tbcbank.retrocache

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Invocation

class RetroCacheInterceptor(
    private val retroCacheManager: RetroCacheManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val invocation = request.tag(Invocation::class.java)

        val cacheAnnotation = invocation?.method()?.getAnnotation(Cache::class.java)
        val cachePolicy: CachePolicy? = invocation?.let {
            it.arguments().find { arg -> arg is CachePolicy } as? CachePolicy
        }

        val key = request.url.toString() + generateRequestBodyHashCode(request.body)

        return when {
            cacheAnnotation != null -> {
                val fromCache = cachePolicy != CachePolicy.Refresh
                val cachedData = retroCacheManager[key].takeIf { fromCache }
                if (cachedData != null) {
                    Response.Builder()
                        .message("From RetroCache")
                        .request(request)
                        .code(cachedData.responseCode)
                        .body(cachedData.responseJson?.toResponseBody())
                        .protocol(cachedData.responseProtocol)
                        .headers(Headers.headersOf(*cachedData.responseHeaders))
                        .build()
                } else {
                    val response = chain.proceed(request)
                    if (response.isSuccessful && response.body != null) {
                        retroCacheManager[key] = RetroCacheValue(
                            responseJson = response
                                .peekBody(retroCacheManager.maxObjectSizeBytes.toLong())
                                .string(),
                            responseCode = response.code,
                            responseProtocol = response.protocol,
                            responseHeaders = response.headers.toArray(),
                            tag = cacheAnnotation.tag.ifEmpty { null },
                            scope = cacheAnnotation.scope.ifEmpty { null },
                            expirationTime = getCacheTime(cacheAnnotation, cachePolicy),
                        )
                    }
                    response
                }
            }

            else -> {
                chain.proceed(request)
            }
        }
    }

    private fun Headers.toArray(): Array<String> {
        val headerList = mutableListOf<String>()
        forEach {
            headerList.add(it.first)
            headerList.add(it.second)
        }
        return headerList.toTypedArray()
    }

    private fun getCacheTime(cacheAnnotation: Cache, cachePolicy: CachePolicy?): Long? {
        if (cachePolicy is CachePolicy.CachedWithTime) {
            return System.currentTimeMillis() + cachePolicy.timeInMillis
        }
        return if (cacheAnnotation.cacheTimeMillis == -1L) {
            null
        } else {
            System.currentTimeMillis() + cacheAnnotation.cacheTimeMillis
        }
    }

    private fun generateRequestBodyHashCode(requestBody: RequestBody?): String {
        return if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val requestBodyString = buffer.readUtf8()
            "-" + requestBodyString.hashCode().toString()
        } else {
            ""
        }
    }

}
