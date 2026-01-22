package com.nuda.nudaclient.presentation.login

import android.content.Intent
import android.media.session.MediaSession
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.api.RetrofitInstance.signupService
import com.nuda.nudaclient.databinding.ActivityLoginBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.signup.SignupAccountActivity
import com.nuda.nudaclient.presentation.signup.SignupDeliveryActivity
import com.nuda.nudaclient.presentation.signup.SignupSurveyActivity

class LoginActivity : AppCompatActivity() {

    // 뷰 바인딩
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 회원가입 버튼 클릭 시 draft 임시 저장 생성
        binding.tvRegister.setOnClickListener {
            val signupToken = TokenManager.getSignupToken(this)
            if(signupToken.isNullOrEmpty()) {
                createDraft()
            } else {
                getDraft()
            }
        }
    }

    // 현재 Draft 데이터 가져오기


    // 현재 Draft 조회
    private fun getDraft() {
        val signupToken = TokenManager.getSignupToken(this)

        signupService.getDraft(signupToken)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        when(body.data.currentStep) {
                            "ACCOUNT" -> navigateToAccount()
                            "DELIVERY" -> navigateToDelivery()
                            "SURVEY" -> navigateToSurvey()
                            "COMPLETED" -> {
                                Toast.makeText(this, "회원가입 완료", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        // 서버 실패 응답
                        createDraft() // draft 및 토큰 재생성
                        Toast.makeText(this, "응답 성공 / 서버 fail - Draft 조회 실패", Toast.LENGTH_LONG).show()
                    }
                },
                onError = { errorMessage ->
                    createDraft()
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            )
    }

    // 임시 저장 draft 생성
    private fun createDraft() {
        signupService.createDraft()
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        // 회원가입 토큰 저장
                        TokenManager.saveSignupToken(this, body.data.signupToken)

                        // 어느 화면으로 이동할지 결정
                        when(body.data.currentStep) {
                            "ACCOUNT" -> navigateToAccount()
                        }
                    } else {
                        // 서버 실패 응답
                        Toast.makeText(this, "Draft 생성 실패", Toast.LENGTH_LONG).show()
                    }
                }
            )
    }

    // 기본정보 입력 화면으로 이동
    private fun navigateToAccount() {
        val intent = Intent(this, SignupAccountActivity::class.java)
        startActivity(intent)
    }

    // 배송정보 입력 화면으로 이동
    private fun navigateToDelivery() {
        val intent = Intent(this, SignupDeliveryActivity::class.java)
        startActivity(intent)
    }
    // 설문조사 화면으로 이동
    private fun navigateToSurvey() {
        val intent = Intent(this, SignupSurveyActivity::class.java)
        startActivity(intent)
    }


}