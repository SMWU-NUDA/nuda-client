package com.nuda.nudaclient.data.remote

import android.content.Context
import android.content.Intent
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.dto.auth.AuthReissueRequest
import com.nuda.nudaclient.presentation.login.LoginActivity
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

        // 응답 변수로 저장
        val response = chain.proceed(newRequest)

        // access 토큰 만료 HTTP 상태 코드(재발급 필요)
        if (response.code == 401) {
            response.close() // 기존 응답 닫기

            // refresh 토큰으로 access 토큰 재발급
            val newAccessToken = tryReissueToken()

            if (newAccessToken != null) {
                // [추가] 재발급 성공 → 원래 요청을 새 토큰으로 재시도
                // 사용자는 이 과정을 전혀 모름 (자동 처리)
                val retryRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
                return chain.proceed(retryRequest)
            } else {
                // 재발급 실패 (refresh도 만료) → 로그인 화면으로
                redirectToLogin()
            }
        }

        return response
    }

    // access 토큰 재발급
    private fun tryReissueToken(): String? {
        return try {
            val refreshToken = TokenManager.getRefreshToken(context)
            if (refreshToken.isNullOrEmpty()) return null

            val response = RetrofitClient.authService
                .reissue(AuthReissueRequest(refreshToken))
                .execute() // 동기 실행

            if (response.isSuccessful && response.body()?.success == true) {
                val newAccessToken = response.body()?.data?.accessToken
                val newRefreshToken = response.body()?.data?.refreshToken
                // 새로 받은 토큰 저장
                TokenManager.saveTokens(context, newAccessToken, newRefreshToken)
                newAccessToken // 새 access 토큰 반환
            } else {
                // 재발급 API 자체가 실패 (refresh 토큰 만료 등)
                TokenManager.clearAuthToken(context)
                null
            }
        } catch (e: Exception) {
            // 네트워크 오류 등
            null
        }
    }

    // 로그인 화면으로 강제 이동
    private fun redirectToLogin() {
        TokenManager.clearAuthToken(context)
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }

    // access 토큰 불필요 API 엔드포인트
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