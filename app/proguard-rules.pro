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
