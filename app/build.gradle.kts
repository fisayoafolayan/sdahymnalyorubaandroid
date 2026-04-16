import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sdahymnal.yoruba"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sdahymnal.yoruba"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load local.properties for optional overrides
        val localProps = rootProject.file("local.properties")
        val props = if (localProps.exists()) {
            Properties().apply { load(localProps.inputStream()) }
        } else Properties()

        // Inject Sentry DSN (keeps it out of version control)
        manifestPlaceholders["sentryDsn"] = props.getProperty("sentry.dsn", "")

        // Analytics config — defaults can be overridden in local.properties
        buildConfigField("String", "ANALYTICS_ENDPOINT",
            "\"${props.getProperty("analytics.endpoint", "https://analytics.afolayan.com/api/send")}\"")
        buildConfigField("String", "ANALYTICS_WEBSITE_ID",
            "\"${props.getProperty("analytics.website_id", "20dd9029-2ed1-4919-a60d-ae74d89795c1")}\"")
        buildConfigField("String", "ANALYTICS_HOSTNAME",
            "\"${props.getProperty("analytics.hostname", "android.sdahymnalyoruba.com")}\"")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val ksPassword = System.getenv("KEYSTORE_PASSWORD")
            if (ksPassword != null) {
                storeFile = file("release.keystore")
                storePassword = ksPassword
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (System.getenv("KEYSTORE_PASSWORD") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.sentry.android.core)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.navigation.testing)
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
