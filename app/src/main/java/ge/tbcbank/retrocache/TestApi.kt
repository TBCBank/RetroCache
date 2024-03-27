package ge.tbcbank.retrocache

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TestApi {

    @Cache(cacheTimeMillis = 60_000)
    @GET("search" + "/{query}/{page}")
    suspend fun getBookWithPage(
        @Path("page") page: String,
        @Path("query") query: String = "Algo",
        @CacheControl cachePolicy: CachePolicy = CachePolicy.Cached
    ): Response<BookPageDto>

    companion object {
        const val cacheTime = 10 * 60 * 1000L
    }
}