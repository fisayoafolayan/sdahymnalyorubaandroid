package com.sdahymnal.yoruba.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
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

    // Derives from shared base - reuses connection pool and dispatcher,
    private val client = HttpClient.base.newBuilder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = "application/json".toMediaType()

    fun trackPageView(url: String) {
        send(buildPayload(url = url))
    }

    fun trackEvent(name: String) {
        send(buildPayload(url = "/", name = "android_$name"))
    }

    private fun buildPayload(url: String, name: String? = null): String {
        val payload = buildMap {
            put("website", JsonPrimitive(WEBSITE_ID))
            put("hostname", JsonPrimitive(HOSTNAME))
            put("url", JsonPrimitive(url))
            put("language", JsonPrimitive("yo"))
            if (name != null) put("name", JsonPrimitive(name))
        }
        return JsonObject(mapOf(
            "type" to JsonPrimitive("event"),
            "payload" to JsonObject(payload),
        )).toString()
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
