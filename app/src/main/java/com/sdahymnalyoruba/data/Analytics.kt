package com.sdahymnalyoruba.data

import com.sdahymnalyoruba.BuildConfig
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Lightweight Umami analytics client.
 * Sends events to the same Umami instance as the web app.
 * Endpoint, website ID, and hostname are configurable via local.properties.
 */
object Analytics {

    private val ENDPOINT = BuildConfig.ANALYTICS_ENDPOINT
    private val WEBSITE_ID = BuildConfig.ANALYTICS_WEBSITE_ID
    private val HOSTNAME = BuildConfig.ANALYTICS_HOSTNAME

    private val client = HttpClient.base.newBuilder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val USER_AGENT =
        "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}; " +
        "${android.os.Build.MODEL}) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Mobile Safari/537.36 SDAHymnalYoruba/1.0"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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
        if (!ENDPOINT.startsWith("https://")) return
        scope.launch {
            try {
                val request = Request.Builder()
                    .url(ENDPOINT)
                    .post(body.toRequestBody(json))
                    .header("User-Agent", USER_AGENT)
                    .build()
                client.newCall(request).execute().close()
            } catch (_: IOException) {
                // Network failures for fire-and-forget telemetry are expected; don't report.
            } catch (e: Exception) {
                Sentry.captureException(e)
            }
        }
    }
}
