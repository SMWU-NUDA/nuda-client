package com.nuda.nudaclient.data.remote

import android.content.Context
import com.nuda.nudaclient.data.remote.api.AuthService
import com.nuda.nudaclient.data.remote.api.IngredientsService
import com.nuda.nudaclient.data.remote.api.MembersService
import com.nuda.nudaclient.data.remote.api.ProductsService
import com.nuda.nudaclient.data.remote.api.ReviewsService
import com.nuda.nudaclient.data.remote.api.SignupService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Retrofit 싱글턴
object RetrofitClient {
    // BASE URL (나중에 도메인 주소로 변경)
    private const val BASE_URL = "http://3.39.59.98:8080/"

    // 앱 전체 context를 받아올 변수 생성
    private lateinit var appContext : Context

    // Application에서 호출할 초기화 함수
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // OkHTTPClient 설정 (한글 인코딩 + 로깅 + 타임아웃)
    private val okHttpClient : OkHttpClient by lazy {
        OkHttpClient.Builder()
            // interceptor 추가 및 context 전달
            .addInterceptor(TokenInterceptor(appContext))
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
            .client(okHttpClient) // OkHTTPClient 설정
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

    // 회원
    val membersService : MembersService by lazy {
        retrofit.create(MembersService::class.java)
    }

    // 리뷰
    val reviewsService : ReviewsService by lazy {
        retrofit.create(ReviewsService::class.java)
    }

    // 상품
    val productsService : ProductsService by lazy {
        retrofit.create(ProductsService::class.java)
    }

    // 성분
    val ingredientsService : IngredientsService by lazy {
        retrofit.create(IngredientsService::class.java)
    }
}