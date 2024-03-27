package ge.tbcbank.retrocache

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class FakeServer {
    private val mockWebServer = MockWebServer()

    val baseEndpoint
        get() = mockWebServer.url("/test/")

    fun shutdown() {
        mockWebServer.dispatcher.shutdown()
    }

    fun setResponse(code: Int, body: String) {
        val response = MockResponse()
            .setResponseCode(code)
            .setBody(body)
        mockWebServer.enqueue(response)
    }
}