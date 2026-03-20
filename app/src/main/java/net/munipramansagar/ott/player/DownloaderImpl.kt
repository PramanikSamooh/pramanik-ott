package net.munipramansagar.ott.player

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import java.util.concurrent.TimeUnit

class DownloaderImpl private constructor() : Downloader() {

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .build()

    override fun execute(request: org.schabi.newpipe.extractor.downloader.Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val requestBuilder = Request.Builder()
            .url(url)
            .method(
                httpMethod,
                dataToSend?.toRequestBody()
            )

        // Add headers
        for ((key, values) in headers) {
            for (value in values) {
                requestBuilder.addHeader(key, value)
            }
        }

        val response = client.newCall(requestBuilder.build()).execute()

        val responseBody = response.body?.string() ?: ""
        val latestUrl = response.request.url.toString()
        val responseCode = response.code
        val responseMessage = response.message
        val responseHeaders = response.headers.toMultimap()

        return Response(
            responseCode,
            responseMessage,
            responseHeaders,
            responseBody,
            latestUrl
        )
    }

    companion object {
        private var instance: DownloaderImpl? = null

        @JvmStatic
        fun getInstance(): DownloaderImpl {
            if (instance == null) {
                instance = DownloaderImpl()
            }
            return instance!!
        }
    }
}
