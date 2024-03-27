package ge.tbcbank.retrocache

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.util.logging.Level
import java.util.logging.Logger

class RetroCacheManager private constructor(
    val maxMemorySizeBytes: Int,
    val maxObjectSizeBytes: Int
) {

    private var enableLogger: Boolean = false

    private val lruCache = object : LruCache<String, RetroCacheValue>(maxMemorySizeBytes) {
        override fun sizeOf(key: String, value: RetroCacheValue): Int {
            val sizeInBytes: Int
            try {
                sizeInBytes = sizeOfObject(value)
                if (sizeInBytes > maxObjectSizeBytes) {
                    throw RetroCacheException(
                        "Response body is too big to cache:" +
                                "\nMax size \"$maxObjectSizeBytes\" bytes," +
                                "\nObject size $sizeInBytes bytes"
                    )
                }
            } catch (e: IOException) {
                throw RetroCacheException("unable to determine object's size: $e")
            }
            return sizeInBytes
        }
    }

    operator fun get(key: String): RetroCacheValue? {
        val retroCacheModel = lruCache[key]
        if (retroCacheModel?.expirationTime != null &&
            retroCacheModel.expirationTime < System.currentTimeMillis()
        ) {
            lruCache.remove(key)
            return null
        }
        return retroCacheModel
    }

    operator fun set(key: String, retroCacheModel: RetroCacheValue) {
        try {
            lruCache.put(key, retroCacheModel)
        } catch (e: RetroCacheException) {
            if (enableLogger) {
                Logger.getLogger("RetroCacheManager").log(Level.WARNING, e.message)
            }
        }
    }

    fun remove(key: String) {
        lruCache.remove(key)
    }

    fun clearAllByTag(tag: String) {
        lruCache.snapshot().forEach {
            if (it.value.tag == tag) lruCache.remove(it.key)
        }
    }

    fun clearAll() {
        lruCache.evictAll()
    }

    fun snapshot() = lruCache.snapshot()

    @Throws(IOException::class)
    private fun sizeOfObject(`object`: Any?): Int {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(`object`)
        objectOutputStream.flush()
        objectOutputStream.close()
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return byteArray.size
    }

    class Builder {
        private var maxMemorySize = DEFAULT_MAX_MEMORY_SIZE_B
        private var maxObjectSize = DEFAULT_MAX_OBJECT_SIZE_B
        private var enableLogger = false

        fun maxMemorySize(sizeBytes: Int): Builder {
            require(sizeBytes > 0) { "Memory size must be greater than 0" }
            this.maxMemorySize = sizeBytes
            return this
        }

        fun maxObjectSize(sizeBytes: Int): Builder {
            require(sizeBytes > 0) { "Object size must be greater than 0" }
            this.maxObjectSize = sizeBytes
            return this
        }

        fun enableLogger(enable: Boolean): Builder {
            enableLogger = enable
            return this
        }

        fun build(): RetroCacheManager {
            return RetroCacheManager(
                maxMemorySize, maxObjectSize
            ).apply {
                enableLogger = this@Builder.enableLogger
            }
        }

        companion object {
            private const val DEFAULT_MAX_MEMORY_SIZE_B = 512 * 50 * 1024
            private const val DEFAULT_MAX_OBJECT_SIZE_B = 512 * 1024
        }
    }
}