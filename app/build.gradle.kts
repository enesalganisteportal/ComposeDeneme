plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt.android)
    id("kotlin-kapt")
    id("com.chaquo.python")
}

android {
    namespace = "com.algan.composedeneme"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.algan.composedeneme"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64", "x86")
            //abiFilters.add("arm64-v8a")
            //abiFilters.add("x86_64")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    flavorDimensions += "pyVersion"
}

chaquopy {
    productFlavors {
        //getByName("py310") { version = "3.10" }
        //getByName("py311") { version = "3.11" }
    }
    defaultConfig {
        // buildPython("C:/Users/dan_a/AppData/Local/Programs/Python/Python312/python.exe")
        buildPython("C:\\Users\\Enes\\AppData\\Local\\Programs\\Python\\Python312\\python.exe")
        pip {
            // A requirement specifier, with or without a version number:
            install("pytubefix")
        }
    }
    productFlavors { }
    sourceSets {
        getByName("main") {
            srcDir("src/main/python")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation(libs.mobile.ffmpeg.audio)
    implementation(libs.androidx.documentfile)
    implementation(libs.kotlinx.coroutines.android)
}
