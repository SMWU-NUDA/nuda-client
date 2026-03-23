package com.nuda.nudaclient.presentation.common.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.RetrofitClient.authService
import com.nuda.nudaclient.data.remote.dto.auth.AuthReissueRequest
import com.nuda.nudaclient.databinding.ActivityMainBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.login.LoginActivity
import com.nuda.nudaclient.presentation.navigation.NavigationActivity
import com.nuda.nudaclient.utils.CustomToast

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 앱 진입 시 access 토큰 확인
        checkAccessToken()

    }

    // access 토큰 검증
    private fun checkAccessToken() {
        authService.validateAccessToken()
            .executeWithHandler(
                context = this,
                skipGlobalTokenHandler = true, // 공통 처리 스킵하고 직접 처리
                onSuccess = { body ->
                    if (body.success == true) { // access 토큰 존재 -> 홈화면 이동
                        // 홈 화면으로 이동
                        startActivity(Intent(this, NavigationActivity::class.java))
                        finish()
                        Log.d("API_DEBUG", "[$TAG] access토큰 인증 완료, 홈화면으로 이동")
                    } else {
                        Log.e("API_ERROR", "[$TAG] access 토큰 검증 응답 성공, 서버 fail")
                    }
                },
                onError = { errorResponse -> // access 토큰 만료 등의 에러
                    if (errorResponse != null) {
                        when (errorResponse?.code) {
                            "AUTH_REQUIRED" -> { // access 토큰 없음 -> 로그인 필요
                                CustomToast.show(binding.root, errorResponse.message)
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                                Log.d("API_DEBUG", "[$TAG] access 토큰 없음, 로그인 화면으로 이동")
                            }
                            "AUTH_EXPIRED_TOKEN" -> { // access 토큰 만료 -> 재발급
                                // access 토큰 재발급 API 호출
                                reissueAccessToken()
                                Log.d("API_DEBUG", "[$TAG] access 토큰 만료, 재발급 API 호출")
                            }
                            "AUTH_INVALID_ACCESS_TOKEN" -> { // 유효하지 않은 access 토큰 -> 로그인 필요
                                CustomToast.show(binding.root, errorResponse.message)
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                                Log.d("API_DEBUG", "[$TAG] 유효하지 않은 access 토큰, 로그인 화면으로 이동")
                            }
                            else -> {
                                // 그 외 서버 에러
                                CustomToast.show(binding.root, errorResponse.message)
                                Log.e("API_ERROR", "[$TAG] 예상치 못한 에러: ${errorResponse.code}")
                            }
                        }
                    } else {
                        // errorResponse가 null인 경우 (파싱 실패)
                        CustomToast.show(binding.root, "서버 오류가 발생했습니다")
                        Log.e("API_ERROR", "[$TAG] errorResponse 파싱 실패")
                    }
                }
            )
    }

    // access 토큰 재발급
    private fun reissueAccessToken() {
        authService.reissue(AuthReissueRequest(TokenManager.getRefreshToken(this)))
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        // 토큰 저장
                        TokenManager.saveTokens(
                            this,
                            body.data?.accessToken,
                            body.data?.refreshToken)
                        // 홈 화면으로 이동
                        startActivity(Intent(this, NavigationActivity::class.java))
                        finish()

                        Log.d("API_DEBUG", "[$TAG] 토큰 재발급 성공, 홈 화면 이동")
                    } else {
                        CustomToast.show(binding.root, "토큰 재발급 실패")
                        // 로그인 화면으로 이동
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        Log.e("API_ERROR", "[$TAG] 토큰 재발급 실패, 서버 응답: ${body.message}")
                    }
                },
                onError = { errorResponse ->
                    if (errorResponse?.code == "AUTH_INVALID_REFRESH_TOKEN") {
                        CustomToast.show(binding.root, errorResponse.message)
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        Log.e("API_ERROR", "[$TAG] 유효하지 않은 refresh 토큰, 로그인 화면으로 이동")
                    } else {
                        // refresh 토큰 관련 그 외 에러도 로그인으로
                        CustomToast.show(binding.root, "인증이 만료됐습니다. 다시 로그인해주세요.")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()

                        Log.e("API_ERROR", "[$TAG] refresh 토큰 재발급 중 예상치 못한 에러: ${errorResponse?.code}") // [추가]
                    }
                }
            )

    }

}