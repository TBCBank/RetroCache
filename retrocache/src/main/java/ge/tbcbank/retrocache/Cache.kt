package ge.tbcbank.retrocache

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cache(
    val tag: String = "",
    val scope: String = "",
    val cacheTimeMillis: Long = -1,
)