# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.sdahymnal.yoruba.data.**$$serializer { *; }
-keepclassmembers class com.sdahymnal.yoruba.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.sdahymnal.yoruba.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp 5.x - keep public API and platform adapters
-keep class okhttp3.OkHttpClient { *; }
-keep class okhttp3.Request { *; }
-keep class okhttp3.Response { *; }
-keep class okhttp3.MediaType { *; }
-keep class okhttp3.RequestBody { *; }
-keep class okhttp3.ResponseBody { *; }
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Sentry - keep classes used via manifest metadata and reflection
-keep class io.sentry.android.core.SentryAndroid { *; }
-keep class io.sentry.android.core.SentryAndroidOptions { *; }
-keepnames class io.sentry.** { *; }
-dontwarn io.sentry.**
