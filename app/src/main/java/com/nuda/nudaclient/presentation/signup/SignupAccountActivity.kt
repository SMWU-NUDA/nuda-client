package com.nuda.nudaclient.presentation.signup

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.api.RetrofitInstance
import com.nuda.nudaclient.data.remote.dto.auth.AuthEmailVerificationRequest
import com.nuda.nudaclient.data.remote.dto.auth.AuthVerifyEmailRequest
import com.nuda.nudaclient.data.remote.dto.signup.SignupAccountRequest
import com.nuda.nudaclient.databinding.ActivitySignupAccountBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.highlightInvalidField
import com.nuda.nudaclient.extensions.setupValidation
import com.nuda.nudaclient.presentation.login.LoginActivity

class SignupAccountActivity : AppCompatActivity() {

    // 뷰 바인딩 객체 선언
    lateinit var binding : ActivitySignupAccountBinding

    // API 서비스 객체 선언
    private val authService = RetrofitInstance.authService
    private val signupService = RetrofitInstance.signupService

    // EditText 유효성 상태 저장 (실시간)
    private var isNicknameValid = false
    private var isUsernameValid = false
    private var isPwValid = false
    private var isPwCheckValid = false
    private var isEmailValid = false
    private var isEmailCertifyValid = false

    // 버튼 클릭 서버 통신 상태 저장
    private var isNicknameAvailable = false
    private var isUsernameAvailable = false
    private var isEmailSendSuccess = false
    private var isEmailVerified = false

    // 뷰 참조
    // EditText
    private lateinit var et_nickname : EditText
    private lateinit var et_username : EditText
    private lateinit var et_pw : EditText
    private lateinit var et_pwCheck : EditText
    private lateinit var et_email : EditText
    private lateinit var et_emailCertify : EditText

    // TextView
    private lateinit var tv_validNickname : TextView
    private lateinit var tv_duplicateNickname : TextView
    private lateinit var tv_validUsername : TextView
    private lateinit var tv_duplicateUsername : TextView
    private lateinit var tv_validPw1 : TextView
    private lateinit var tv_validPw2 : TextView
    private lateinit var tv_validPwCheck : TextView
    private lateinit var tv_validEmail : TextView
    private lateinit var tv_emailCertify : TextView

    // Button
    private lateinit var btn_checkNickname : Button
    private lateinit var btn_checkUsername : Button
    private lateinit var btn_sendEmail : Button
    private lateinit var btn_certifyEmail : Button
    private lateinit var btn_nextPage : Button

    // ImageView - 버튼으로 사용
    private lateinit var iv_back : ImageView
    private lateinit var iv_pwVisible : ImageView
    private lateinit var iv_pwCheckVisible : ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 뷰 바인딩 설정
        binding = ActivitySignupAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 뷰 참조 초기화
        et_nickname = binding.etNickname
        et_username = binding.etUsername
        et_pw = binding.etPw
        et_pwCheck = binding.etPwCheck
        et_email = binding.etEmail
        et_emailCertify = binding.etEmailCertify

        tv_validNickname = binding.tvValidNickname
        tv_duplicateNickname = binding.tvDuplicateNickname
        tv_validUsername = binding.tvValidId
        tv_duplicateUsername = binding.tvDuplicateId
        tv_validPw1 = binding.tvValidPw1
        tv_validPw2 = binding.tvValidPw2
        tv_validPwCheck = binding.tvValidPwCheck
        tv_validEmail = binding.tvValidEmail
        tv_emailCertify = binding.tvValidEmailCertify

        btn_checkNickname = binding.btnCheckNickname
        btn_checkUsername = binding.btnCheckId
        btn_sendEmail = binding.btnSendEmail
        btn_certifyEmail = binding.btnCertifyEmail
        btn_nextPage = binding.btnNextPage

        iv_back = binding.toolBar.ivBack
        iv_pwVisible = binding.ivVisiblePw
        iv_pwCheckVisible = binding.ivVisiblePwCheck

        // TextWatcher 설정
        setupTextWatchers()

        // 버튼 클릭 이벤트
        setButtons()
    }

    // TextWatcher 설정 (EditText 실시간 검사)
    private fun setupTextWatchers() {
        // 닉네임
        et_nickname.setupValidation(
            tv_validNickname,
            validator = { it.length in 4..10 }, // it : EditText에 입력된 텍스트
            onValidationChanged = { isValid ->
                isNicknameValid = isValid  // 받은 isValid를 isNicknameValid에 저장
                isNicknameAvailable = false // 닉네임 중복 확인 후 EditText 수정 시 중복 확인 상태 초기화
                tv_duplicateNickname.text = "" // 중복 확인 메세지도 초기화
            }
        )

        // 아이디
        et_username.setupValidation(
            tv_validUsername,
            validator = { it.length in 4..16 },
            onValidationChanged = { isValid ->
                isUsernameValid = isValid
                isUsernameAvailable = false
                tv_duplicateUsername.text = ""
            }
        )

        // 비밀번호
        et_pw.doAfterTextChanged { text ->
            val input = text.toString()
            et_pw.setBackgroundResource(R.drawable.et_input_default)
            when {
                input.isEmpty() -> {
                    setPasswordValidationColor(R.color.gray3, R.color.gray3)
                    isPwValid = false
                } else -> {
                    val validationResult = validatePassword(input)
                    when(validationResult) {
                        1 -> {  // 조건 1, 2 만족
                            setPasswordValidationColor(R.color.green, R.color.green)
                            isPwValid = true
                        }
                        2 -> { // 조건 1 만족
                            setPasswordValidationColor(R.color.green, R.color.red)
                            isPwValid = false
                        }
                        3 -> { // 조건 2 만족
                            setPasswordValidationColor(R.color.red, R.color.green)
                            isPwValid = false
                        }
                        4 -> {  // 조건 1, 2 불만족
                            setPasswordValidationColor(R.color.red, R.color.red)
                            isPwValid = false
                        }
                    }
                }
            }

            // 비밀번호 확인 입력창에서 비밀번호 입력 X 조건에 걸렸을 경우 나온 텍스트 초기화
            if (et_pwCheck.text.isNullOrEmpty()) {
                tv_validPwCheck.text = ""
            }
        }

        // 비밀번호 확인
        et_pwCheck.doAfterTextChanged { text ->
            val input = text.toString()
            val password = et_pw.text.toString() // 비밀번호 입력 x -> 빈 문자열 반환

            et_pwCheck.setBackgroundResource(R.drawable.et_input_default)

            when {
                // 비밀번호 입력 X or 비밀번호 유효성 검사 실패
                !isPwValid -> {
                    tv_validPwCheck.text = getString(R.string.valid_pw_check_noPW)
                    tv_validPwCheck.setTextColor(ContextCompat.getColor(this, R.color.red))
                    isPwCheckValid = false
                }
                // 비밀번호 확인 입력 X
                input.isEmpty() -> {
                    isPwCheckValid = false
                }
                // 비밀번호와 비밀번호 확인 텍스트가 일치하는 경우
                input == password -> {
                    tv_validPwCheck.text = getString(R.string.valid_pw_check_true)
                    tv_validPwCheck.setTextColor(ContextCompat.getColor(this, R.color.green))
                    isPwCheckValid = true
                }
                else -> {
                    tv_validPwCheck.text = getString(R.string.valid_pw_check_false)
                    tv_validPwCheck.setTextColor(ContextCompat.getColor(this, R.color.red))
                    isPwCheckValid = false
                }
            }
        }

        // 이메일
        et_email.setupValidation(
            tv_validEmail,
            validator = { isValidEmail(it) },
            onValidationChanged = { isValid ->
                isEmailValid = isValid
                isEmailSendSuccess = false
                tv_validEmail.text = ""
            }
        )

        // 이메일 인증번호
        et_emailCertify.setupValidation(
            tv_emailCertify,
            validator = { it.length == 6 },
            onValidationChanged = { isValid ->
                isEmailCertifyValid = isValid
                isEmailVerified = false
                tv_emailCertify.text = ""
            }
        )

    }

    // 이메일 형식 검증 함수
    private fun isValidEmail(email : String) : Boolean {
        // Android에서 제공하는 이메일 형식 검증 패턴. true/false 반환
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 비밀번호 유효성 검사 상태 함수 (조건 2개)
    private fun validatePassword(password: String) : Int {
        // 조건 1 : 8자 이상
        val isLengthValid : Boolean = password.length >= 8

        //조건 2 : 영문 대/소문자, 숫자, 특수문자 중 2개 이상
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        val typeCount = listOf(hasUpperCase, hasLowerCase, hasDigit, hasSpecial).count {it}
        val isTypeValid : Boolean = typeCount >= 2

        when {
            isLengthValid && isTypeValid -> return 1 // 조건 1, 2 만족
            isLengthValid && !isTypeValid -> return 2 // 조건 1 만족
            !isLengthValid && isTypeValid -> return 3 // 조건 2 만족
            !isLengthValid && !isTypeValid -> return 4 // 조건 1, 2 불만족
            else -> return 0 // 에러
        }
    }

    // 비밀번호 헬퍼 함수
    private fun setPasswordValidationColor(color1:Int, color2:Int) {
        tv_validPw1.setTextColor(ContextCompat.getColor(this, color1))
        tv_validPw2.setTextColor(ContextCompat.getColor(this,color2))
    }

    // 버튼 설정
    private fun setButtons() {
        setupNicknameDuplicateCheck()
        setupUsernameDuplicateCheck()
        setupPwVisible()
        setupEmailSend()
        setupEmailCertify()
        setupNextPage()
        setupBack()
    }

    // 닉네임 중복 확인 버튼
    private fun setupNicknameDuplicateCheck() {
        btn_checkNickname.setOnClickListener {
            // 1. 닉네임 입력값 유효성 검사
            if(!isNicknameValid) {
                // 2. 텍스트 색 변경 후 리스너 종료
                tv_validNickname.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener 
            }

            // 3. 닉네임 중복 확인 API 호출 (isNicknameValid = true)
            authService.getNickname(et_nickname.text.toString())
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body?.success == true) { // 서버의 success 필드
                            tv_duplicateNickname.text = getString(R.string.btnValid_nickname_true)
                            tv_duplicateNickname.setTextColor(ContextCompat.getColor(this@SignupAccountActivity, R.color.green))
                            isNicknameAvailable = true // 중복 확인 상태 저장
                        } else { // HTTP 200인데 서버에서 실패 응답
                            // body?.success == false 이거나 null일 수 있음.
                            tv_duplicateNickname.text = body?.data ?: body?.message
                                    ?: getString(R.string.btnValid_nickname_false)
                            tv_duplicateNickname.setTextColor(ContextCompat.getColor(this@SignupAccountActivity, R.color.red))
                            isNicknameAvailable = false
                        }
                    }
                )
        }
    }

    // 아이디 중복 확인 버튼
    private fun setupUsernameDuplicateCheck() {
        btn_checkUsername.setOnClickListener {
            // 1. 아이디 입력값 유효성 검사
            if(!isUsernameValid) {
                // 2. 텍스트 색 변경 후 리스너 종료
                tv_validUsername.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }

            // 3. 아이디 중복 확인 API 호출 (isNicknameValid = true)
            authService.getUsername(et_username.text.toString())
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) { // 서버의 success 필드
                            // 진짜 성공
                            tv_duplicateUsername.text = getString(R.string.btnValid_id_true)
                            tv_duplicateUsername.setTextColor(
                                ContextCompat.getColor(
                                    this@SignupAccountActivity,
                                    R.color.green
                                )
                            )
                            isUsernameAvailable = true // 중복 확인 상태 저장
                        } else { // HTTP 200인데 서버에서 실패 응답
                            // body?.success == false 이거나 null일 수 있음.
                            tv_duplicateNickname.text =
                                body?.data ?: body?.message ?: getString(R.string.btnValid_id_false)
                            tv_duplicateNickname.setTextColor(
                                ContextCompat.getColor(
                                    this@SignupAccountActivity,
                                    R.color.red
                                )
                            )
                            isUsernameAvailable = false
                        }
                    }
                )
        }
    }

    // 이메일 인증번호 보내기 버튼
    private fun setupEmailSend() {
        btn_sendEmail.setOnClickListener {
            if(!isEmailValid) { // 이메일 유효성 검사 실패
                tv_validEmail.text = getString(R.string.btnValid_email_false)
                tv_validEmail.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }

            // 이메일 인증번호 보내기 API 호출 (isEmailValid = true)
            // 요청 객체 생성
            val request = AuthEmailVerificationRequest(email = et_email.text.toString())
            // API 호출
            authService.requestEmailVerification(request)
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if(body.success == true) { // 진짜 성공
                            // 이메일 인증번호 보내기 성공 시 유효성 검사 텍스트 변경
                            tv_validEmail.text = getString(R.string.btnValid_email_true)
                            tv_validEmail.setTextColor(ContextCompat.getColor(this@SignupAccountActivity,R.color.green))
                            isEmailSendSuccess = true // 중복 확인 상태 저장
                        } else { // HTTP 200인데 서버에서 실패 응답
                            // body?.success == false
                            tv_validEmail.text = body?.data ?: body?.message ?: getString(R.string.btnValid_email_fail)
                            tv_validEmail.setTextColor(ContextCompat.getColor(this@SignupAccountActivity,R.color.red))
                            isEmailSendSuccess = false
                        }
                    }
                )
        }
    }

    // 이메일 인증번호 인증하기 버튼
    private fun setupEmailCertify() {
        btn_certifyEmail.setOnClickListener {
            // 이메일 입력 및 인증번호 전송 확인
            if(!isEmailSendSuccess) {
                tv_validEmail.text = getString(R.string.btnValid_email_certify_noEmail)
                tv_validEmail.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }
            // 인증번호 유효성 검사(6자리) 실패
            if(!isEmailCertifyValid) {
                tv_emailCertify.text = getString(R.string.btnValid_email_certify_empty)
                tv_emailCertify.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }

            // 이메일 인증번호 인증하기 API 호출 (isEmailCertifyValid = true)
            val request = AuthVerifyEmailRequest(
                code = et_emailCertify.text.toString(),
                email = et_email.text.toString()
            )
            authService.verifyEmail(request)
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if(body.success == true) { // 진짜 성공
                            tv_emailCertify.text = getString(R.string.btnValid_email_certify_true)
                            tv_emailCertify.setTextColor(ContextCompat.getColor(this@SignupAccountActivity,R.color.green))
                            isEmailVerified = true // 중복 확인 상태 저장
                        } else { // HTTP 200인데 서버에서 실패 응답
                            // body?.success == false 이거나 null일 수 있음.
                            tv_emailCertify.setTextColor(ContextCompat.getColor(this@SignupAccountActivity,R.color.red))
                            isEmailVerified = false
                        }
                    },
                    onError = { errorMessage ->
                        tv_emailCertify.text = getString(R.string.btnValid_email_certify_false)
                        tv_emailCertify.setTextColor(ContextCompat.getColor(this@SignupAccountActivity,R.color.red))
                        isEmailVerified = false
                    }
                )
        }
    }

    // 비밀번호, 비밀번호 확인 버튼
    private fun setupPwVisible() {
        var isPwVisible = false
        var isPwCheckVisible = false

        // 비밀번호
        iv_pwVisible.setOnClickListener {
            isPwVisible = !isPwVisible

            if(isPwVisible) { // true
                // 비밀번호 보이기
                et_pw.transformationMethod = HideReturnsTransformationMethod.getInstance()
                // 아이콘 색 변경
                iv_pwVisible.setColorFilter(
                    ContextCompat.getColor(this, R.color.gray2)
                )
            } else { // false
                // 비밀번호 숨기기
                et_pw.transformationMethod = PasswordTransformationMethod.getInstance()
                // 아이콘 색 변경 (원래대로)
                iv_pwVisible.setColorFilter(
                    ContextCompat.getColor(this,R.color.gray4)
                )
            }

            // 입력창 커서 맨 뒤로
            et_pw.setSelection(et_pw.text?.length ?: 0) // setSelection은 Int를 필요로 함
        }

        // 비밀번호 확인
        iv_pwCheckVisible.setOnClickListener {
            isPwCheckVisible = !isPwCheckVisible

            if(isPwCheckVisible) { // true
                // 비밀번호 확인 보이기
                et_pwCheck.transformationMethod = HideReturnsTransformationMethod.getInstance()
                // 아이콘 색 변경
                iv_pwCheckVisible.setColorFilter(
                    ContextCompat.getColor(this, R.color.gray2)
                )
            } else { // false
                // 비밀번호 확인 숨기기
                et_pwCheck.transformationMethod = PasswordTransformationMethod.getInstance()
                // 아이콘 색 변경 (원래대로)
                iv_pwCheckVisible.setColorFilter(
                    ContextCompat.getColor(this,R.color.gray4)
                )
            }

            // 입력창 커서 맨 뒤로
            et_pwCheck.setSelection(et_pwCheck.text?.length ?: 0)
        }
    }

    // 다음 페이지 이동 버튼
    private fun setupNextPage() {
        // 다음 버튼 클릭 이벤트 처리
        btn_nextPage.setOnClickListener {
            if(!validationAccount()) {
                highlightInvalidFields()
                return@setOnClickListener
            }
            updateAccountData()
        }
    }

    // 뒤로가기 버튼
    private fun setupBack() {
        iv_back.setOnClickListener {
            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    // 다음 버튼 클릭 시 유효성 검사 확인
    private fun validationAccount() : Boolean {
        return isNicknameAvailable &&
                isUsernameAvailable &&
                isPwValid &&
                isPwCheckValid &&
                isEmailVerified
    }

    // 유효성 검사 실페 시 입력창 배경 변경
    private fun highlightInvalidFields() {
        et_nickname.highlightInvalidField(isNicknameAvailable)
        et_username.highlightInvalidField(isUsernameAvailable)
        et_pw.highlightInvalidField(isPwValid)
        et_pwCheck.highlightInvalidField(isPwCheckValid)
        et_email.highlightInvalidField(isEmailVerified)
        et_emailCertify.highlightInvalidField(isEmailVerified)
    }

    // 유효성 검사 통과 시 계정정보 데이터 업데이트
    private fun updateAccountData() {
        // 서버 통신 (API 호출)
        // 회원가입 토큰 저장
        val signupToken = TokenManager.getSignupToken(this)
        val signupAccountRequest = SignupAccountRequest(
            email = et_email.text.toString().trim(),
            nickname = et_nickname.text.toString().trim(),
            password = et_pw.text.toString().trim(),
            username = et_username.text.toString().trim()
        )
        signupService.updateAccount(signupToken, signupAccountRequest)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        // 임시 토스트 메세지
                        Toast.makeText(this, body.data, Toast.LENGTH_LONG).show()
                        // 회원가입 2단계 페이지로 이동 (SignupDeliveryActivity)
                        val intent = Intent(this, SignupDeliveryActivity::class.java)
                        startActivity(intent)
                    } else {
                        // 임시 토스트 메세지
                        Toast.makeText(this, body.message, Toast.LENGTH_LONG).show()
                    }
                }
            )
    }
}