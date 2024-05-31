# RetroCache

Kotlin library that provides caching capabilities for Retrofit requests. With this library, you can
annotate your Retrofit service methods to specify caching behavior for responses, enhancing the
performance and efficiency of your network calls.

### Add library to the project

project level gradle:

```gradle
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

module level gradle:

```gradle
dependencies {
    implementation("com.github.TBCBank:RetroCache:1.0.2")
}
```

> Project will be added to Maven Central in future

## Setup

#### Create RetroCacheManager object

This class uses an LRU cache mechanism. When the maximum memory size is reached, it automatically
removes the least accessed objects to accommodate new entries.
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

#### Scoping cache

```kotlin
@Cache(scope = "logged_in_user_scope") // specify scope in annotation
@GET("fetch")
suspend fun getData(): Response<Data>


retroCacheManager.clearScopeCache(scope = "logged_in_user_scope") // will remove all objects which are in "logged_in_user_scope" scope
```

> You may create different scopes in your application, like application_scope or user_scope 
> and clear after logout for example, to not save previous user data
