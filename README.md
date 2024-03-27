# RetroCache

Kotlin library that provides caching capabilities for Retrofit requests. With this library, you can annotate your Retrofit service methods to specify caching behavior for responses, enhancing the performance and efficiency of your network calls.

### Add library to the project
[Download latest version [1.0] JAR file](https://github.com/TBCBank/RetroCache/releases/download/v1.0/retrocache-1.0.jar) and add it in module's /libs directory, then implement in build.gradle.kts:
```gradle
dependencies {
    implementation(files("libs/retrocache-1.0.jar"))
}
```
> Project will be added to Maven Central in future


## Setup

#### Create RetroCacheManager object
This class uses an LRU cache mechanism. When the maximum memory size is reached, it automatically removes the least accessed objects to accommodate new entries.
The cache will automatically clear upon application termination.
```kotlin
    RetroCacheManager.Builder()
            .maxMemorySize(512 * 60 * 1024) // Maximum size of memory in bytes
            .maxObjectSize(512 * 1024) // If object will be more than 512kb, it won't be added to the cache
            .enableLogger(true)
            .build()
```
> In most cases you need to provide [RetroCacheManager] as Singleton

#### When building Okhttp object, pass RetroCacheInterceptor
```kotlin
    OkHttpClient.Builder()
            .addInterceptor(RetroCacheInterceptor(retroCacheManager))
            .build()
```

## Features

#### Annotation-based caching for Retrofit service methods
```kotlin
    @Cache
    @GET("fetch")
    suspend fun getData(): Response<Data>
```

#### Control cache with tags
```kotlin
    @Cache(tag = "tag1")
    @GET("fetch")
    suspend fun getData(): Response<Data>
```

#### Define cache refresh time in milliseconds
```kotlin
    @Cache(cacheTimeMillis = 60_000)
    @GET("fetch")
    suspend fun getData(): Response<Data>
```

#### Dynamic control of caching and refreshing
```kotlin
    @Cache
    @GET("fetch")
   suspend fun getData(
        @CacheControl cachePolicy: CachePolicy = CachePolicy.Refresh // will refresh data
    ): Response<Data>
```

#### Clearing cache

```kotlin
    retroCacheManager.clearAllByTag("tag1") // will remove all objects with corresponding tag

    retroCacheManager.clearAll() // will remove all objects from the cache
```
> You may use [retroCacheManager.clearAll()] in [onLowMemory()] of application
> ```kotlin
>    override fun onLowMemory() {
>        super.onLowMemory()
>        retroCacheManager.clearAll()
>    }
>```
