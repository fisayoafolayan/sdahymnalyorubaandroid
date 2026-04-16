package com.sdahymnal.yoruba.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Lightweight Umami analytics client.
 * Sends events to the same Umami instance as the web app.
 */
object Analytics {

    private const val ENDPOINT = "https://analytics.afolayan.com/api/send"
    private const val WEBSITE_ID = "20dd9029-2ed1-4919-a60d-ae74d89795c1"
    private const val HOSTNAME = "android.sdahymnalyoruba.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = "application/json".toMediaType()

    fun trackPageView(url: String) {
        send("""{"type":"event","payload":{"website":"$WEBSITE_ID","hostname":"$HOSTNAME","url":"$url","language":"yo"}}""")
    }

    fun trackEvent(name: String) {
        send("""{"type":"event","payload":{"website":"$WEBSITE_ID","hostname":"$HOSTNAME","url":"/","name":"android_$name","language":"yo"}}""")
    }

    private fun send(body: String) {
        scope.launch {
            try {
                val request = Request.Builder()
                    .url(ENDPOINT)
                    .post(body.toRequestBody(json))
                    .header("User-Agent", "SDAHymnalYoruba-Android/1.0")
                    .build()
                client.newCall(request).execute().close()
            } catch (_: Exception) {
                // Analytics should never crash the app
            }
        }
    }
}
