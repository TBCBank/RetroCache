package ge.tbcbank.retrocache.di

import ge.tbcbank.retrocache.TestApi
import ge.tbcbank.retrocache.RetroCacheInterceptor
import ge.tbcbank.retrocache.RetroCacheManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CoreModule {

    val api by lazy {
        provideRetrofit(
            provideOkHttpClient(
            RetroCacheInterceptor(
                retroCacheManager
            )
        )
        ).create(TestApi::class.java)
    }

    val retroCacheManager by lazy {
        RetroCacheManager.Builder()
            .maxMemorySize(Runtime.getRuntime().maxMemory().toInt() / 8)
            .maxObjectSize(512 * 1024)
            .enableLogger(true)
            .build()
    }

    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.itbook.store/1.0/")
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