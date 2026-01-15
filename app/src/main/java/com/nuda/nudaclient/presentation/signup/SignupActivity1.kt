package com.nuda.nudaclient.presentation.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivitySignup1Binding
import com.nuda.nudaclient.extensions.setupValidation
import com.nuda.nudaclient.presentation.login.LoginActivity

class SignupActivity1 : AppCompatActivity() {

    // 뷰 바인딩 객체 선언
    lateinit var binding : ActivitySignup1Binding

    // EditText 유효성 상태 저장 (실시간)
    private var isNicknameValid = false
    private var isUsernameValid = false
    private var isPwValid = false
    private var isPwCheckValid = false
    private var isEmailValid = false
    private var isEmailCertifyValid = false

    // 버튼 클릭 유효성 검사 상태 저장 (서버 통신)
    private var isNicknameDuplicated = false
    private var isUsernameDupliicated = false
    private var isEamilVerified = false

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
        binding = ActivitySignup1Binding.inflate(layoutInflater)
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
            onValidationChanged = { isValid -> isNicknameValid = isValid } // 받은 isValid를 isNicknameValid에 저장
        )

        // 아이디
        et_username.setupValidation(
            tv_validUsername,
            validator = { it.length in 4..16 },
            onValidationChanged = { isValid -> isUsernameValid = isValid }
        )

        // 비밀번호
        et_pw.doAfterTextChanged { text ->
            val input = text.toString()
            when {
                input.isEmpty() -> {
                    tv_validPw1.setTextColor(ContextCompat.getColor(this, R.color.gray3))
                    tv_validPw2.setTextColor(ContextCompat.getColor(this, R.color.gray3))
                    isPwValid = false
                }
                // 조건 1, 2 만족
                validatePassword(input) == 1 -> {
                    tv_validPw1.setTextColor(ContextCompat.getColor(this, R.color.green))
                    tv_validPw2.setTextColor(ContextCompat.getColor(this,R.color.green))
                    isPwValid = true
                }
                // 조건 1 만족
                validatePassword(input) == 2 -> {
                    tv_validPw1.setTextColor(ContextCompat.getColor(this, R.color.green))
                    tv_validPw2.setTextColor(ContextCompat.getColor(this,R.color.red))
                    isPwValid = false
                }
                // 조건 2 만족
                validatePassword(input) == 3 -> {
                    tv_validPw1.setTextColor(ContextCompat.getColor(this, R.color.red))
                    tv_validPw2.setTextColor(ContextCompat.getColor(this,R.color.green))
                    isPwValid = false
                }
                // 조건 1, 2 불만족
                validatePassword(input) == 4 -> {
                    tv_validPw1.setTextColor(ContextCompat.getColor(this, R.color.red))
                    tv_validPw2.setTextColor(ContextCompat.getColor(this,R.color.red))
                    isPwValid = false
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
            onValidationChanged = { isValid -> isEmailValid = isValid },
            emptyMessage = "",
            validMessage = "",
            invalidMessage = ""
        )

        // 이메일 인증번호
        et_emailCertify.setupValidation(
            tv_emailCertify,
            validator = { it.length == 6 },
            onValidationChanged = { isValid -> isEmailCertifyValid = isValid },
            emptyMessage = "",
            validMessage = "",
            invalidMessage = ""
        )

    }

    // 이메일 형식 검증 함수
    private fun isValidEmail(email : String) : Boolean {
        // Android에서 제공하는 이메일 형식 검증 패턴. true/false 반환
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
//            // 이메일 인증번호 보내기 성공 시 유효성 검사 텍스트 변경
//            tv_validEmail.text = getString(R.string.btnValid_email_true)
//            tv_validEmail.setTextColor(ContextCompat.getColor(this,R.color.green))

        }
    }

    // 이메일 인증번호 인증하기 버튼
    private fun setupEmailCertify() {
        btn_certifyEmail.setOnClickListener {
            // 인증번호 유효성 검사(6자리) 실패
            if(!isEmailCertifyValid) {
                tv_emailCertify.text = getString(R.string.btnValid_email_certify_empty)
                tv_emailCertify.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }

            // 이메일 인증번호 인증하기 API 호출 (isEmailCertifyValid = true)

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

    // 페이지 이동 버튼(다음)
    private fun setupNextPage() {
        // 다음 버튼 클릭 이벤트 처리
        btn_nextPage.setOnClickListener {
            // 1. 모든 항목 유효성 검사 통과
            if (isNicknameValid && isUsernameValid && isPwValid && isPwCheckValid && isEmailValid && isEmailCertifyValid) {
                // 2. 서버 통신 (API 호출)

                // 3. 회원가입 2단계 페이지로 이동 (SignupActivity2)
                val intent = Intent(this, SignupActivity2::class.java)
                startActivity(intent)
            }
            else {
                // 4. 유효성 검사 실패 시, 실패한 항목 editText 테두리 색 변경(red)
            }
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
}