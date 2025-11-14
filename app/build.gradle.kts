plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "me.talofa.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "me.talofa.app"
        minSdk = 24  // Android 7.0+
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.3"
        
        // Better app metadata
        setProperty("archivesBaseName", "talofa-v$versionName")
    }

    buildTypes {
        release {
            // Enable code shrinking, obfuscation, and optimization
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // For signing, you'll need to create a keystore:
            // keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias share-button
            // Then uncomment and configure:
            // signingConfig = signingConfigs.getByName("release")
        }
        
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
        }
    }
    
    // Optional: Add signing configuration for release builds
    // signingConfigs {
    //     create("release") {
    //         storeFile = file("release-key.jks")
    //         storePassword = System.getenv("KEYSTORE_PASSWORD")
    //         keyAlias = "share-button"
    //         keyPassword = System.getenv("KEY_PASSWORD")
    //     }
    // }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
