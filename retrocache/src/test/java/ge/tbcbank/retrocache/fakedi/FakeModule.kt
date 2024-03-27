package ge.tbcbank.retrocache.fakedi

import ge.tbcbank.retrocache.FakeServer
import ge.tbcbank.retrocache.RetroCacheInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FakeModule {

    var fakeServer: FakeServer? = null

    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(fakeServer?.baseEndpoint!!)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideOkHttpClient(
        retroCacheInterceptor: RetroCacheInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(retroCacheInterceptor)
            .build()
    }
}