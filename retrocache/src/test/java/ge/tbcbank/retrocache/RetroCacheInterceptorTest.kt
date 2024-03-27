package ge.tbcbank.retrocache

import ge.tbcbank.retrocache.fakedi.FakeApi
import ge.tbcbank.retrocache.fakedi.FakeBody
import ge.tbcbank.retrocache.fakedi.FakeModule
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class RetroCacheInterceptorTest {
    private lateinit var api: FakeApi

    private lateinit var fakeServer: FakeServer

    private lateinit var retroCacheManager: RetroCacheManager

    @Before
    fun init() {
        fakeServer = FakeServer()
        FakeModule.fakeServer = fakeServer
        retroCacheManager = RetroCacheManager.Builder()
            .maxMemorySize(512 * 10 * 1024)
            .maxObjectSize(512 * 1024)
            .enableLogger(true)
            .build()

        api = FakeModule.provideRetrofit(
            FakeModule.provideOkHttpClient(
                RetroCacheInterceptor(
                    retroCacheManager
                )
            )
        ).create(FakeApi::class.java)
    }

    @After
    fun tearDown() {
        fakeServer.shutdown()
    }

    @Test
    fun `response is cached`() = runTest {
        fakeServer.setResponse(HttpURLConnection.HTTP_OK, """{"value": "1"}""".trimIndent())
        val result1 = api.fetchDataOnlyAnnotation().body()
        assertEquals("1", result1?.value)
        assertNotNull(retroCacheManager[getBaseEndpoint()])
    }

    @Test
    fun `response is gotten from cache`() = runTest {
        fakeServer.setResponse(HttpURLConnection.HTTP_OK, """{"value": "1"}""".trimIndent())
        api.fetchDataOnlyAnnotation().body()
        fakeServer.setResponse(HttpURLConnection.HTTP_OK, """{"value": "2"}""".trimIndent())
        val result2 = api.fetchDataOnlyAnnotation().body()
        assertEquals("1", result2?.value) // value is still "1"
    }

    @Test
    fun `response is refreshed if Refresh is passed as @CacheControl`() = runTest {
        fakeServer.setResponse(HttpURLConnection.HTTP_OK, """{"value": "1"}""".trimIndent())
        api.fetchDataWithDynamicControl(CachePolicy.Cached).body()
        fakeServer.setResponse(HttpURLConnection.HTTP_OK, """{"value": "2"}""".trimIndent())
        val result2 = api.fetchDataWithDynamicControl(CachePolicy.Refresh).body()
        assertEquals("2", result2?.value)
    }

    @Test
    fun `key is created with url and body if body is present`() = runTest {
        fakeServer.setResponse(HttpURLConnection.HTTP_OK, """{"value": "1"}""".trimIndent())
        api.fetchDataWithBody(FakeBody("testBody11")).body()
        retroCacheManager.snapshot().forEach {
            // pattern that checks if contains "-" and numbers at the end, which is string to UTF8 result
            val pattern = Regex("^.*-\\d+$")
            assertTrue(pattern.matches(it.key))
        }
    }

    private fun getBaseEndpoint() = fakeServer.baseEndpoint.toString() + "fetch"
}