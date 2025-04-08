plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
}

android {
    namespace = "com.example.picktimeapp"
    compileSdk = 34  // Updated from 33 to 34

    defaultConfig {
        applicationId = "com.example.picktimeapp"
        minSdk = 28
        targetSdk = 33  // Can keep this as 33 if you want
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"  // Updated to match Kotlin 1.9.0
    }

    // Add this to resolve potential issues with kapt and Hilt
    kapt {
        correctErrorTypes = true
    }

    // assets 디렉토리 추가
    sourceSets["main"].assets.srcDirs("src/main/assets")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)


    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.foundation.android)
    implementation(project(":opencv"))

//    implementation(libs.androidx.navigation.runtime.android)
    kapt(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0") // jetpack 전용 hilt 연동 라이브러리

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Icon
    implementation(libs.androidx.material.icons.extended)

    // DataStore
    implementation(libs.androidx.datastore.preferences)


    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // WebSocket
    implementation(libs.websocket)

    // Google Auth
//    implementation(libs.google.auth)
    // mic 권한 허용
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.navigation.compose) //  navigation-compose 안정 버전
    implementation(libs.lifecycle.runtime.compose) // lifecycle-runtime 안정 버전
    implementation(libs.lifecycle.viewmodel.compose) //  viewmodel-compose 안정 버전


    // 이미지 url을 받을 때 사용
    implementation(libs.coil.compose)

    // 주파수 분석 관련
    implementation("com.github.wendykierp:JTransforms:3.1")

    // ai
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.tensorflow.lite.support)

    //chord JSON을 파싱할 때 사용 , Gson 직접 사용을 위한 의존성
    implementation("com.google.code.gson:gson:2.10.1")

//    implementation(libs.mediapipe.vision)

}