import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Load configuration from app.properties
val appProps = Properties()
file("app.properties").inputStream().use { appProps.load(it) }
val defaultScheme = appProps.getProperty("DEFAULT_SCHEME")
val defaultUrl = appProps.getProperty("DEFAULT_URL")

android {
    namespace = "me.talofa.webview"
    compileSdk = 34
    
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "me.talofa.webview"
        minSdk = 24  // Android 7.0+
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.3"
        
        setProperty("archivesBaseName", "talofa-webview-v$versionName")
        
        // Inject values as build config fields
        buildConfigField("String", "DEFAULT_SCHEME", "\"$defaultScheme\"")
        buildConfigField("String", "DEFAULT_URL", "\"$defaultUrl\"")
        
        // Inject into manifest
        manifestPlaceholders["defaultScheme"] = defaultScheme
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
}
