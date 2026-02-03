plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.nuda.nudaclient"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.nuda.nudaclient"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }


    buildFeatures {
        viewBinding = true // 뷰 바인딩 설정 true
        buildConfig = true // 빌드 설정 true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit
    implementation(libs.retrofit)
    // GSON 컨버터
    implementation(libs.converter.gson)

    // OkHttp (한글 인코딩용)
    implementation(libs.okhttp3)
    // 로깅 인터셉터
    implementation(libs.logging.interceptor)

    // 주소 찾기 webview
    implementation(libs.webview)

    // gson
    implementation(libs.gson)

    // flexbox
    implementation(libs.flexbox)
}