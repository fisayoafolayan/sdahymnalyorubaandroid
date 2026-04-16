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

# OkHttp 5.x
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Sentry
-keep class io.sentry.** { *; }
-keepnames class io.sentry.** { *; }
-dontwarn io.sentry.**
