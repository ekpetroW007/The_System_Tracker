import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
val mapkitApiKey = localProperties.getProperty("MAPKIT_API_KEY", "")
val requestedRelease = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }
val signingProperties = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
fun signingValue(environment: String, property: String): String =
    providers.environmentVariable(environment).orNull ?: signingProperties.getProperty(property, "")
val releaseStoreFile = signingValue("THE_SYSTEM_KEYSTORE", "storeFile")
val releaseStorePassword = signingValue("THE_SYSTEM_STORE_PASSWORD", "storePassword")
val releaseKeyAlias = signingValue("THE_SYSTEM_KEY_ALIAS", "keyAlias")
val releaseKeyPassword = signingValue("THE_SYSTEM_KEY_PASSWORD", "keyPassword")
val releaseSigningConfigured = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all(String::isNotBlank)
val targetAbi = providers.gradleProperty("targetAbi").orNull
    ?: providers.gradleProperty("arm64Only").orNull?.takeIf { it == "true" }?.let { "arm64-v8a" }

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.personal.thesystem"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.personal.thesystem"
        minSdk = 26
        targetSdk = 36
        versionCode = 16
        versionName = "2.0.0"

        buildConfigField("String", "MAPKIT_API_KEY", "\"${mapkitApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    splits {
        abi {
            isEnable = targetAbi != null
            reset()
            targetAbi?.let { include(*arrayOf(it)) }
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

if (requestedRelease) {
    require(mapkitApiKey.isNotBlank()) {
        "MAPKIT_API_KEY is required for release builds. Add it to local.properties."
    }
    require(releaseSigningConfigured || providers.gradleProperty("allowUnsignedRelease").orNull == "true") {
        "Permanent release signing is required. Configure keystore.properties or THE_SYSTEM_KEYSTORE credentials."
    }
}

providers.environmentVariable("THE_SYSTEM_BUILD_DIR").orNull?.let { root ->
    layout.buildDirectory.set(file(root).resolve("app"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("com.yandex.android:maps.mobile:4.42.0-full")

    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
}
