package ge.tbcbank.retrocache.fakedi

import ge.tbcbank.retrocache.Cache
import ge.tbcbank.retrocache.CacheControl
import ge.tbcbank.retrocache.CachePolicy
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FakeApi {

    @Cache
    @GET("fetch")
    suspend fun fetchDataOnlyAnnotation(): Response<FakeData>

    @Cache
    @GET("fetch")
    suspend fun fetchDataWithDynamicControl(
        @CacheControl cachePolicy: CachePolicy
    ): Response<FakeData>

    @Cache
    @POST("fetch")
    suspend fun fetchDataWithBody(
        @Body fakeBody: FakeBody
    ): Response<FakeData>
}

data class FakeData(val value: String)

data class FakeBody(val value: String)