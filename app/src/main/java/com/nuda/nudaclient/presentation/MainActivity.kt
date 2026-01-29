package com.nuda.nudaclient.presentation

import android.content.Intent
import android.media.session.MediaSession
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.local.UserPreferences
import com.nuda.nudaclient.data.remote.RetrofitClient.authService
import com.nuda.nudaclient.data.remote.dto.auth.AuthLoginResponse
import com.nuda.nudaclient.data.remote.dto.auth.AuthReissueRequest
import com.nuda.nudaclient.databinding.ActivityMainBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.login.LoginActivity
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

    private fun checkAccessToken() {
        authService.validateAccessToken()
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) { // access 토큰 존재 -> 홈화면 이동
                        // 응답 데이터 저장
                        val meResponse = AuthLoginResponse.Data.MeResponse (
                            id = body.data.id,
                            username = body.data.username,
                            nickname = body.data.nickname,
                            profileImg = body.data.profileImg,
                            email = body.data.email
                        )
                        UserPreferences.saveUserInfo(this, meResponse)

                        // 홈 화면으로 이동
//                        startActivity(Intent(this, HomeActivity::class.java))
                    } else {
                        when (body.code) {
                            "AUTH_REQUIRED" -> { // access 토큰 없음 -> 로그인 필요
                                CustomToast.show(binding.root, body.message)
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                            "AUTH_EXPIRED_TOKEN" -> { // access 토큰 만료 -> 재발급
                                CustomToast.show(binding.root, body.message)
                                // access 토큰 재발급 API 호출
                                reissueAccessToken()
                                // 토큰 재확인
                                validateAccessToken()
                            }
                            "AUTH_INVALID_ACCESS_TOKEN" -> { // 유효하지 않은 access 토큰 -> 로그인 필요
                                CustomToast.show(binding.root, body.message)
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                        }
                    }
                }
            )
    }

    private fun reissueAccessToken() {
        authService.reissue(AuthReissueRequest(TokenManager.getRefreshToken(this)))
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        TokenManager.saveTokens(this, body.data.accessToken, body.data.refreshToken)
                        CustomToast.show(binding.root, "토큰 재발급 성공")
                    } else {
                        CustomToast.show(binding.root, "토큰 재발급 실패")
                    }
                }
            )

    }

    private fun validateAccessToken() {
        authService.validateAccessToken()
    }

}