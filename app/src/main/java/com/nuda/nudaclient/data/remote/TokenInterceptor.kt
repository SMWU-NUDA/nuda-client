package com.nuda.nudaclient.data.remote

import android.content.Context
import com.nuda.nudaclient.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request() // retrofit이 만든 원본 http 요청 객체

        // 특정 경로는 토큰 없이 요청
        val path = originalRequest.url.encodedPath
        if (shouldSkipAuth(path)) {
            return chain.proceed(originalRequest)
        }

        val accessToken = TokenManager.getAccessToken(context)

        // 토큰이 없으면 원본 요청 그대로 실행
        if (accessToken.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // API 요청 헤더에 토큰 추가
        val newRequest = originalRequest.newBuilder() // 헤더에 토큰을 추가한 요청 객체 생성
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(newRequest)
    }

    private fun shouldSkipAuth(path: String) : Boolean {
        return path.contains("/auth/reissue") || // 경로 문자열에 해당 값이 포함되면 true
                path.contains("/auth/login") ||
                path.contains("/auth/emails/verifications") ||
                path.contains("/auth/emails/verification-requests") ||
                path.contains("/auth/search?username={username}") ||
                path.contains("/auth/search?nickname={nickname}") ||
                path.contains("/signup") // 회원가입 전체
    }
}