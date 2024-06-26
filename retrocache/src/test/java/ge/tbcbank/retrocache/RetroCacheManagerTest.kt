package ge.tbcbank.retrocache

import okhttp3.Protocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class RetroCacheManagerTest {

    @Test
    fun `get returns cached object when present`() {
        val cacheManager = RetroCacheManager.Builder().build()
        val model = getRetroCacheModel()
        cacheManager["key"] = model

        val cachedModel = cacheManager["key"]

        assertEquals(model, cachedModel)
    }

    @Test
    fun `get returns null when object expired`() {
        val cacheManager = RetroCacheManager.Builder().build()
        val model = RetroCacheValue(
            responseJson = "test",
            responseCode = 200,
            responseProtocol = Protocol.HTTP_1_1,
            responseHeaders = arrayOf(),
            tag = "123",
            expirationTime = System.currentTimeMillis() - 1000
        )
        cacheManager["key"] = model

        val cachedModel = cacheManager["key"]

        assertNull(cachedModel)
    }

    @Test
    fun `set adds object to cache`() {
        val cacheManager = RetroCacheManager.Builder().build()

        val model = getRetroCacheModel()
        cacheManager["key"] = model

        val cachedModel = cacheManager["key"]
        assertEquals(model, cachedModel)
    }

    @Test
    fun `remove removes object from cache`() {
        val cacheManager = RetroCacheManager.Builder().build()
        val model = getRetroCacheModel()
        cacheManager["key"] = model

        cacheManager.remove("key")

        val cachedModel = cacheManager["key"]
        assertNull(cachedModel)
    }

    private fun getRetroCacheModel(
        responseJson: String = "test",
        tag: String = "123",
        scope: String = ""
    ) = RetroCacheValue(
        responseJson = responseJson,
        responseCode = 200,
        responseProtocol = Protocol.HTTP_1_1,
        responseHeaders = arrayOf(),
        tag = tag,
        scope = scope
    )

    @Test
    fun `clearAll removes all objects from cache`() {
        val cacheManager = RetroCacheManager.Builder().build()
        cacheManager["key1"] = getRetroCacheModel("test1", "123")
        cacheManager["key2"] = getRetroCacheModel("test2", "456")

        cacheManager.clearAll()

        assertNull(cacheManager["key1"])
        assertNull(cacheManager["key2"])
    }

    @Test
    fun `clearAllByTag removes objects with specified tag`() {
        val cacheManager = RetroCacheManager.Builder().build()
        cacheManager["key1"] = getRetroCacheModel("test1", tag = "tag1")
        cacheManager["key2"] = getRetroCacheModel("test2", tag = "tag1")
        cacheManager["key3"] = getRetroCacheModel("test3", tag = "tag2")

        cacheManager.clearAllByTag("tag1")

        assertNull(cacheManager["key1"])
        assertNull(cacheManager["key2"])
        assertEquals("test3", cacheManager["key3"]?.responseJson)
    }

    @Test
    fun `clearScope removes objects within specific scope`() {
        val cacheManager = RetroCacheManager.Builder().build()
        val scope = "user"
        cacheManager["key1"] = getRetroCacheModel("test1", tag = "tag1", scope = scope)
        cacheManager["key2"] = getRetroCacheModel("test2", tag = "tag1", scope = scope)
        cacheManager["key3"] = getRetroCacheModel("test3", tag = "tag2", scope = "otherScope")

        cacheManager.clearScopeCache(scope = scope)

        assertNull(cacheManager["key1"])
        assertNull(cacheManager["key2"])
        assertEquals("test3", cacheManager["key3"]?.responseJson)
    }

    @Test
    fun `builder sets custom max memory size`() {
        val maxMemorySize = 1024
        val cacheManager = RetroCacheManager.Builder()
            .maxMemorySize(maxMemorySize)
            .build()

        assertEquals(maxMemorySize, cacheManager.maxMemorySizeBytes)
    }

    @Test
    fun `builder sets custom max object size`() {
        val maxObjectSize = 2048
        val cacheManager = RetroCacheManager.Builder()
            .maxObjectSize(maxObjectSize)
            .build()

        assertEquals(maxObjectSize, cacheManager.maxObjectSizeBytes)
    }

    @Test
    fun `last accessed object is moved to top of cache`() {
        val cacheManager = RetroCacheManager.Builder().build()
        cacheManager["key1"] = getRetroCacheModel("test1")
        cacheManager["key2"] = getRetroCacheModel("test2")
        cacheManager["key3"] = getRetroCacheModel("test3")

        var first: RetroCacheValue? = null
        var last: RetroCacheValue? = null
        var i = 0
        cacheManager.snapshot().forEach {
            if (i == 0) first = it.value
            if (i == cacheManager.snapshot().size - 1) last = it.value
            i++
        }
        assertEquals(first?.responseJson, "test1")
        assertEquals(last?.responseJson, "test3")

        cacheManager["key2"] // accessing middle object
        first = null
        last = null
        i = 0
        cacheManager.snapshot().forEach {
            if (i == 0) first = it.value
            if (i == cacheManager.snapshot().size - 1) last = it.value
            i++
        }
        assertEquals(first?.responseJson, "test1")
        assertEquals(last?.responseJson, "test2")
    }

    @Test
    fun `old cached responses are removed if maxMemory limit has reached`() {
        val cacheManager = RetroCacheManager.Builder()
            .maxMemorySize(100 * sizeOfObject(getRetroCacheModel(get1KbString()))) // fits 100 object
            .maxObjectSize(1 * sizeOfObject(getRetroCacheModel(get1KbString())))
            .build()

        (0..110).forEach {// add 10 items more than limit
            cacheManager[it.toString()] = getRetroCacheModel(get1KbString())
        }

        (0..110).forEach {
            if (it <= 10) {
                assertNull(cacheManager[it.toString()]) // first 10 items should be removed
            } else {
                assertNotNull(cacheManager[it.toString()]) // others should stay in cache
            }
        }
    }

    @Test
    fun `too big object is not added to cache`() {
        val cacheManager = RetroCacheManager.Builder()
            .maxMemorySize(100 * 1024) // 100 kb max memory size
            .maxObjectSize(1 * 1024) // 1 kb max memory size
            .build()

        val invalidSizedString = get1KbString() + "12345"
        cacheManager["key"] = getRetroCacheModel(invalidSizedString)
        assertNull(cacheManager["key"])
    }

    private fun sizeOfObject(`object`: Any?): Int {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(`object`)
        objectOutputStream.flush()
        objectOutputStream.close()
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return byteArray.size
    }


    private fun get1KbString(): String {
        val stringBuilder = StringBuilder()
        repeat(1024) {
            stringBuilder.append('a')
        }
        return stringBuilder.toString()
    }
}