package com.nuda.nudaclient.presentation.signup

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.nuda.nudaclient.data.local.SignupDataManager
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.RetrofitClient.authService
import com.nuda.nudaclient.data.remote.RetrofitClient.signupService
import com.nuda.nudaclient.data.remote.dto.auth.AuthEmailVerificationRequest
import com.nuda.nudaclient.data.remote.dto.auth.AuthVerifyEmailRequest
import com.nuda.nudaclient.data.remote.dto.signup.SignupAccountRequest
import com.nuda.nudaclient.databinding.ActivitySignupAccountBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.highlightInvalidField
import com.nuda.nudaclient.extensions.setupPasswordVisible
import com.nuda.nudaclient.extensions.setupValidation
import com.nuda.nudaclient.presentation.login.LoginActivity

class SignupAccountActivity : AppCompatActivity() {

    // TODO: security(auth): (signup) SharedPreferences에서 비밀번호 저장 제거 (회원가입 시 비밀번호는 서버 전송 후 즉시 폐기)

    // 뷰 바인딩 객체 선언
    lateinit var binding : ActivitySignupAccountBinding

    // 회원가입 토큰 저장
    private var signupToken : String? = null

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

    // 복원 모드 플래그 (TextWatcher가 초기화하는 것을 방지)
    private var isRestoringData = false

    // 타이머 관련 변수
    private var countDownTimer: CountDownTimer? = null
    private val TIMER_DURATION = 5 * 60 * 1000L // 5분 (밀리초)

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
    private lateinit var tv_timer : TextView

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

        // 토큰 초기화
        signupToken = TokenManager.getSignupToken(this)

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
        tv_timer = binding.tvTimer

        btn_checkNickname = binding.btnCheckNickname
        btn_checkUsername = binding.btnCheckId
        btn_sendEmail = binding.btnSendEmail
        btn_certifyEmail = binding.btnCertifyEmail
        btn_nextPage = binding.btnNextPage

        iv_back = binding.toolBar.ivBack
        iv_pwVisible = binding.ivVisiblePw
        iv_pwCheckVisible = binding.ivVisiblePwCheck

        // draft 만료 체크
        SignupDataManager.clearExpiredData(this)

        // Draft 데이터 복원
        SignupDataManager.loadPrefData(this)
        setupProcess()

        // TextWatcher 설정
        setupTextWatchers()

        // 버튼 클릭 이벤트
        setButtons()
    }

    override fun onPause() {
        super.onPause()

        saveAccountData()
        SignupDataManager.backupPrefData(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // 타이머 정리
    }

    // 진행상황 복원
    private fun setupProcess() {
        isRestoringData = true // 복원 시작

        // 상태 복원
        isNicknameValid = SignupDataManager.isNicknameValid
        isUsernameValid = SignupDataManager.isUsernameValid
        isPwValid = SignupDataManager.isPwValid
        isPwCheckValid = SignupDataManager.isPwCheckValid
        isEmailValid = SignupDataManager.isEmailValid
        isEmailCertifyValid = SignupDataManager.isEmailCertifyValid
        isNicknameAvailable = SignupDataManager.isNicknameAvailable
        isUsernameAvailable = SignupDataManager.isUsernameAvailable
        isEmailSendSuccess = SignupDataManager.isEmailSendSuccess
        isEmailVerified = SignupDataManager.isEmailVerified

        // 텍스트 복원
        et_nickname.setText(SignupDataManager.nickname ?: "")
        et_username.setText(SignupDataManager.username ?: "")
        et_pw.setText(SignupDataManager.password ?: "")
        et_pwCheck.setText(SignupDataManager.passwordCheck ?: "")
        et_email.setText(SignupDataManager.email ?: "")

        // UI 복원
        // 닉네임
        if (isNicknameValid) {
            tv_validNickname.setTextColor(ContextCompat.getColor(this@SignupAccountActivity, R.color.green))
        }
        // 닉네임 중복 확인
        if (isNicknameAvailable) {
            tv_duplicateNickname.text = getString(R.string.btnValid_nickname_true)
            tv_duplicateNickname.setTextColor(ContextCompat.getColor(this@SignupAccountActivity, R.color.green))
        }
        // 아이디
        if (isUsernameValid) {
            tv_validUsername.setTextColor(ContextCompat.getColor(this@SignupAccountActivity, R.color.green))
        }
        // 아이디 중복 확인
        if (isUsernameAvailable) {
            tv_duplicateUsername.text = getString(R.string.btnValid_id_true)
            tv_duplicateUsername.setTextColor(ContextCompat.getColor(this@SignupAccountActivity, R.color.green))
        }
        // 비밀번호
        if (isPwValid) {
            setPasswordValidationColor(R.color.green, R.color.green)
        }
        // 비밀번호 확인
        if (isPwCheckValid) {
            tv_validPwCheck.text = getString(R.string.valid_pw_check_true)
            tv_validPwCheck.setTextColor(ContextCompat.getColor(this, R.color.green))
        }
        // 이메일 코드 전송 완료
        if (isEmailSendSuccess) {
            tv_validEmail.text = getString(R.string.btnValid_email_true)
            tv_validEmail.setTextColor(ContextCompat.getColor(this@SignupAccountActivity, R.color.green))
        }
        // 이메일 인증 완료
        if (isEmailVerified) {
            tv_emailCertify.text = getString(R.string.btnValid_email_certify_true)
            tv_emailCertify.setTextColor(ContextCompat.getColor(this@SignupAccountActivity,R.color.green))
        }

        isRestoringData = false // 복원 종료

    }


    // TextWatcher 설정 (EditText 실시간 검사)
    private fun setupTextWatchers() {
        // 닉네임
        et_nickname.setupValidation(
            tv_validNickname,
            validator = { it.length in 4..10 }, // it : EditText에 입력된 텍스트
            onValidationChanged = { isValid ->
                isNicknameValid = isValid  // 받은 isValid를 isNicknameValid에 저장
                if (!isRestoringData) { // 복원 모드가 아닐 때만 초기화
                    isNicknameAvailable = false // 닉네임 중복 확인 후 EditText 수정 시 중복 확인 상태 초기화
                    tv_duplicateNickname.text = "" // 중복 확인 메세지도 초기화
                }
            }
        )

        // 아이디
        et_username.setupValidation(
            tv_validUsername,
            validator = { it.length in 4..16 },
            onValidationChanged = { isValid ->
                isUsernameValid = isValid
                if (!isRestoringData) {
                    isUsernameAvailable = false
                    tv_duplicateUsername.text = ""
                }
            }
        )

        // 비밀번호
        et_pw.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
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
            if (isRestoringData) return@doAfterTextChanged
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
                if (!isRestoringData) {
                    isEmailSendSuccess = false
                    tv_validEmail.text = ""
                }
            }
        )

        // 이메일 인증번호
        et_emailCertify.setupValidation(
            tv_emailCertify,
            validator = { it.length == 6 },
            onValidationChanged = { isValid ->
                isEmailCertifyValid = isValid
                if (!isRestoringData) {
                    isEmailVerified = false
                    tv_emailCertify.text = ""
                }
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
            et_nickname.setBackgroundResource(R.drawable.et_input_default)

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
                        if (body.success == true) { // 서버의 success 필드
                            tv_duplicateNickname.text = getString(R.string.btnValid_nickname_true)
                            tv_duplicateNickname.setTextColor(ContextCompat.getColor(this@SignupAccountActivity, R.color.green))
                            isNicknameAvailable = true // 중복 확인 상태 저장
                        } else { // HTTP 200인데 서버에서 실패 응답
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
            et_username.setBackgroundResource(R.drawable.et_input_default)

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
                            tv_duplicateUsername.setTextColor(ContextCompat.getColor(this@SignupAccountActivity, R.color.green))
                            isUsernameAvailable = true // 중복 확인 상태 저장
                        } else { // HTTP 200인데 서버에서 실패 응답
                            // body?.success == false 이거나 null일 수 있음.
                            tv_duplicateUsername.text =
                                body?.data ?: body?.message ?: getString(R.string.btnValid_id_false)
                            tv_duplicateUsername.setTextColor(
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
            et_email.setBackgroundResource(R.drawable.et_input_default)
            et_emailCertify.setBackgroundResource(R.drawable.et_input_default)

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
                            
                            // 이메일 인증번호 타이머 시작
                            startEmailTimer()
                        } else { // HTTP 200인데 서버에서 실패 응답
                            tv_validEmail.text = body.data ?: body.message ?: getString(R.string.btnValid_email_fail)
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
            et_email.setBackgroundResource(R.drawable.et_input_default)
            et_emailCertify.setBackgroundResource(R.drawable.et_input_default)

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
                    onError = { _ ->
                        tv_emailCertify.text = getString(R.string.btnValid_email_certify_false)
                        tv_emailCertify.setTextColor(ContextCompat.getColor(this@SignupAccountActivity,R.color.red))
                        isEmailVerified = false
                    }
                )
        }
    }

    // 비밀번호, 비밀번호 확인 버튼
    private fun setupPwVisible() {
        iv_pwVisible.setupPasswordVisible(
            context = this,
            editText = et_pw
        )
        iv_pwCheckVisible.setupPasswordVisible(
            context = this,
            editText = et_pwCheck
        )
    }

    // 다음 페이지 이동 버튼
    private fun setupNextPage() {
        // 다음 버튼 클릭 이벤트 처리
        btn_nextPage.setOnClickListener {
            if(!validationAccount()) {
                highlightInvalidFields()
                return@setOnClickListener
            }
            // 계정 정보 draft에 업데이트
            updateAccountData()
        }
    }

    // 뒤로가기 버튼
    private fun setupBack() {
        iv_back.setOnClickListener {
            // 작성 계정 정보 및 상태 데이터 저장
            saveAccountData()
            SignupDataManager.backupPrefData(this)
            
            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 이메일 인증번호 타이머
    private fun startEmailTimer() {
        // 기존 타이머가 있으면 취소
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tv_timer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                tv_timer.text = "00:00"
                tv_timer.setTextColor(ContextCompat.getColor(this@SignupAccountActivity,R.color.red))
                isEmailSendSuccess = false // 인증번호 재전송 필요
            }
        }.start()
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

                        // 최신 draft 조회 및 pref 백업
                        backupAccount()

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

    // 최신 draft 조회 및 pref 백업
    private fun backupAccount() {
        // 업데이트 된 최신 draft 조회
        signupService.getDraft(signupToken)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        // 갱신된 draft 유효기간 저장
                        SignupDataManager.expiresAt =  body.data?.expiresAt
                        // 입력한 계정 정보, 유효성 검사 상태 데이터 싱글턴 변수 저장
                        saveAccountData()
                        // pref에 전체 백업
                        SignupDataManager.backupPrefData(this)
                    }
                }
            )
    }

    // 계정 정보 및 유효성 검사 상태 데이터 저장
    private fun saveAccountData() {
        // 입력한 계정 정보
        SignupDataManager.nickname = et_nickname.text.toString().trim()
        SignupDataManager.username = et_username.text.toString().trim()
        SignupDataManager.password = et_pw.text.toString().trim()
        SignupDataManager.passwordCheck = et_pwCheck.text.toString().trim()
        SignupDataManager.email = et_email.text.toString().trim()

        // 유효성 검사 상태
        SignupDataManager.isNicknameValid = isNicknameValid
        SignupDataManager.isUsernameValid = isUsernameValid
        SignupDataManager.isPwValid = isPwValid
        SignupDataManager.isPwCheckValid = isPwCheckValid
        SignupDataManager.isEmailValid = isEmailValid
        SignupDataManager.isEmailCertifyValid = isEmailCertifyValid
        SignupDataManager.isNicknameAvailable = isNicknameAvailable
        SignupDataManager.isUsernameAvailable = isUsernameAvailable
        SignupDataManager.isEmailSendSuccess = isEmailSendSuccess
        SignupDataManager.isEmailVerified = isEmailVerified
    }


}