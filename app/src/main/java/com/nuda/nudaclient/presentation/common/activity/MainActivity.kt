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
import com.nuda.nudaclient.data.local.UserPreferences
import com.nuda.nudaclient.data.remote.RetrofitClient
import com.nuda.nudaclient.data.remote.dto.auth.AuthReissueRequest
import com.nuda.nudaclient.data.remote.dto.common.Me
import com.nuda.nudaclient.databinding.ActivityMainBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.login.LoginActivity
import com.nuda.nudaclient.presentation.navigation.NavigationActivity
import com.nuda.nudaclient.presentation.product.ProductDetailActivity
import com.nuda.nudaclient.utils.CustomToast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

//    // access 토큰
//    val accessToken = TokenManager.getAccessToken(this)

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
        RetrofitClient.authService.validateAccessToken()
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) { // access 토큰 존재 -> 홈화면 이동
                        // 응답 데이터 저장
                        val meResponse = Me(
                            id = body.data?.id ?: -1,
                            username = body.data?.username ?: "null",
                            nickname = body.data?.nickname ?: "null",
                            profileImg = body.data?.profileImg ?: "null",
                            email = body.data?.email ?: "null"
                        )
                        UserPreferences.saveUserInfo(this, meResponse)

                        // 홈 화면으로 이동
                        startActivity(Intent(this, NavigationActivity::class.java))
                        // 테스트 이동
//                        startActivity(Intent(this, ProductDetailActivity::class.java))

                        finish()
                        Log.d("API_DEBUG", "access토큰 인증 완료, 홈화면으로 이동")
                    } else {
                        Log.e("API_ERROR", "응답 성공, 서버 fail ")
                    }
                },
                onError = { errorResponse -> // access 토큰 만료 등의 에러
                    if (errorResponse != null) {
                        when (errorResponse?.code) {
                            "AUTH_REQUIRED" -> { // access 토큰 없음 -> 로그인 필요
                                CustomToast.show(binding.root, errorResponse.message)
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                                Log.d("API_DEBUG", "access 토큰 없음, 로그인 화면으로 이동")
                            }
                            "AUTH_EXPIRED_TOKEN" -> { // access 토큰 만료 -> 재발급
                                CustomToast.show(binding.root, errorResponse.message)
                                // access 토큰 재발급 API 호출
                                reissueAccessToken()
                                Log.d("API_DEBUG", "access 토큰 만료, 재발급 API 호출")
                            }
                            "AUTH_INVALID_ACCESS_TOKEN" -> { // 유효하지 않은 access 토큰 -> 로그인 필요
                                CustomToast.show(binding.root, errorResponse.message)
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                                Log.d("API_DEBUG", "유효하지 않은 access 토큰, 로그인 화면으로 이동")
                            }
                            else -> {
                                // 그 외 서버 에러
                                CustomToast.show(binding.root, errorResponse.message)
                            }
                        }
                    } else {
                        // errorResponse가 null인 경우 (파싱 실패)
                        CustomToast.show(binding.root, "서버 오류가 발생했습니다")
                    }
                }
            )
    }

    private fun reissueAccessToken() {
        RetrofitClient.authService.reissue(AuthReissueRequest(TokenManager.getRefreshToken(this)))
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        TokenManager.saveTokens(this, body.data?.accessToken, body.data?.refreshToken)
                        CustomToast.show(binding.root, "토큰 재발급 성공")
                        Log.d("API_DEBUG", "토큰 재발급 성공, access 토큰 재검증")
                        // 토큰 재확인
                        checkAccessToken()
                    } else {
                        CustomToast.show(binding.root, "토큰 재발급 실패")
                        // 로그인 화면으로 이동
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        Log.d("API_DEBUG", "토큰 재발급 실패")
                    }
                },
                onError = { errorResponse ->
                    if (errorResponse?.code == "AUTH_INVALID_REFRESH_TOKEN") {
                        CustomToast.show(binding.root, errorResponse.message)
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        Log.d("API_DEBUG", "유효하지 않은 refresh 토큰, 로그인 화면으로 이동")
                    }
                }
            )

    }

//    private fun validateAccessToken() {
//        authService.validateAccessToken()
//    }

}