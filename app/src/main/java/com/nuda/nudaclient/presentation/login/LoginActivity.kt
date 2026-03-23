package com.nuda.nudaclient.presentation.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.SignupDataManager
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.RetrofitClient.authService
import com.nuda.nudaclient.data.remote.RetrofitClient.signupService
import com.nuda.nudaclient.data.remote.dto.auth.AuthLoginRequest
import com.nuda.nudaclient.databinding.ActivityLoginBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.highlightInvalidField
import com.nuda.nudaclient.extensions.setupPasswordVisible
import com.nuda.nudaclient.presentation.navigation.NavigationActivity
import com.nuda.nudaclient.presentation.signup.SignupAccountActivity
import com.nuda.nudaclient.utils.CustomToast

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    // 뷰 바인딩
    private lateinit var binding: ActivityLoginBinding

    // 로그인 상태 저장
    private var isIdValid = false
    private var isPwValid = false

    // 뷰 참조
    private lateinit var et_username : EditText
    private lateinit var et_password : EditText


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

        // Draft 만료 체크 및 삭제 (회원가입 토큰 포함)
        SignupDataManager.clearExpiredData(this)

        // 뷰 참조 초기화
        et_username = binding.etLoginID
        et_password = binding.etLoginPW

        // 버튼 설정
        setupButtons()
    }

    // 버튼 설정
    private fun setupButtons() {
        setLoginButton()
        setSignupButton()
        setPasswordVisible()
    }

    // 로그인 버튼
    private fun setLoginButton() {
        binding.btnLogin.setOnClickListener {
            // 아이디, 비밀번호 중 하나라도 입력창이 비어있을 경우
            if(!validationLogin()) {
                et_username.highlightInvalidField(isIdValid)
                et_password.highlightInvalidField(isPwValid)
                return@setOnClickListener
            }
            // 로그인 API 호출
            login()
        }
    }

    // 회원가입 버튼
    private fun setSignupButton() {
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

    // 비밀번호 보기 이미지 버튼
    private fun setPasswordVisible() {
        binding.ivVisiblePw.setupPasswordVisible(
            context = this,
            editText = et_password
        )
    }


    // 로그인 입력 유효성 검사
    private fun validationLogin() : Boolean {
        if (et_username.text.isEmpty()) isIdValid = false
        else isIdValid = true

        if (et_password.text.isEmpty()) isPwValid = false
        else isPwValid = true

        return isIdValid && isPwValid
    }

    // 로그인 API 호출
    private fun login() {
        authService.login(AuthLoginRequest(
            username = et_username.text.toString().trim(),
            password = et_password.text.toString().trim()
        )).executeWithHandler(
            context = this,
            onSuccess = { body->
                if(body.success == true) {
                    // 로그인 응답 토큰 저장
                    TokenManager.saveTokens(this, body.data?.accessToken, body.data?.refreshToken)

                    // 홈화면으로 이동
                    val intent = Intent(this, NavigationActivity::class.java)
                    startActivity(intent)
                    finish()

                    Log.d("API_DEBUG", "[$TAG] 로그인 성공")
                    CustomToast.show(binding.root, "로그인되었습니다")
                }
            },
            onError = { errorResponse ->
                // 이메일, 비밀번호가 틀렸을 경우
                if (errorResponse?.code == "MEMBER_INVALID_CREDENTIALS") {
                    Log.e("API_ERROR", "[$TAG] 로그인 실패: 아이디 또는 비밀번호 틀림")
                    CustomToast.show(binding.root, "아이디 또는 비밀번호가 올바르지 않습니다")
                }
            }
        )

    }

    // 현재 Draft 조회
    private fun getDraft() {
        val signupToken = TokenManager.getSignupToken(this)
        
        signupService.getDraft(signupToken)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    // 현재 draft 조회 성공 (signupToken 유효)
                    if(body.success == true) {
                        // 유효 기간 pref 저장
                        SignupDataManager.expiresAt = body.data?.expiresAt
                        SignupDataManager.backupPrefData(this)

                        // 회원가입 화면 이동
                        when(body.data?.currentStep) {
                            "COMPLETED" -> CustomToast.show(binding.root, "이미 회원가입이 완료되었습니다")
                            else ->  navigateToAccount() // 무조건 첫 번째 계정정보 페이지로 이동
                        }
                    } else {
                        // 서버 실패 응답
                        createDraft() // draft 및 토큰 재생성
                        Log.e("API_ERROR", "[$TAG] draft 조회 실패")
                    }
                },
                onError = { _ ->
                    createDraft()
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
                        TokenManager.saveSignupToken(this, body.data?.signupToken)
                        // 생성된 draft 유효기간 pref에 저장 및 회원가입 화면 이동
                        getDraft()
                    } else {
                        // 서버 실패 응답
                        Log.e("API_ERROR", "[$TAG] draft 생성 실패")
                    }
                }
            )
    }

    // 기본정보 입력 화면으로 이동
    private fun navigateToAccount() {
        val intent = Intent(this, SignupAccountActivity::class.java)
        startActivity(intent)

        Log.d("API_DEBUG", "[$TAG] 회원가입 화면으로 이동")
    }

}