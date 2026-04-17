package com.sdahymnalyoruba.data

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Shared OkHttpClient base. Both HymnRepository and Analytics derive
 * timeout-specific clients via [base].newBuilder(), which reuses the
 * same connection pool and dispatcher threads.
 */
internal object HttpClient {
    val base: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}
