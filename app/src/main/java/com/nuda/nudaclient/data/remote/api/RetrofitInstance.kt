package com.nuda.nudaclient.data.remote.api

import com.nuda.nudaclient.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Retrofit 싱글턴
object RetrofitInstance {
    // BASE URL (나중에 도메인 주소로 변경)
    private const val BASE_URL = "http://3.39.59.98:8080/"

    // 로깅 인터셉터 설정
    private val logging = HttpLoggingInterceptor().apply {
        // 요청과 응답의 본문 내용까지 로그에 포함
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHTTPClient 설정 (한글 인코딩 + 로깅 + 타임아웃)
    private val okHttpClient : OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if(BuildConfig.DEBUG) {
                    addInterceptor(logging)
                }
            }
        // 타임아웃 설정
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit 인스턴스 생성 
    private val retrofit : Retrofit by lazy { // by lazy : 처음 사용할 때 초기화 (지연 초기화)
        // Retrofit 빌더 생성
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Service 인터페이스 구현체 생성
    // 인증
    val authService : AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    // 회원가입
    val signupService : SignupService by lazy {
        retrofit.create(SignupService::class.java)
    }


}