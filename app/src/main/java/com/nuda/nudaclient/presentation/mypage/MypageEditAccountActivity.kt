package com.nuda.nudaclient.presentation.mypage

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.SignupDataManager
import com.nuda.nudaclient.data.remote.RetrofitClient.authService
import com.nuda.nudaclient.data.remote.RetrofitClient.membersService
import com.nuda.nudaclient.data.remote.dto.auth.AuthEmailVerificationRequest
import com.nuda.nudaclient.data.remote.dto.auth.AuthVerifyEmailRequest
import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.members.MembersUserInfoResponse
import com.nuda.nudaclient.databinding.ActivityMypageEditAccountBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.highlightInvalidField
import com.nuda.nudaclient.extensions.setupPasswordVisible
import com.nuda.nudaclient.extensions.setupValidation
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.utils.CustomToast
import kotlin.math.PI

class MypageEditAccountActivity : BaseActivity() {

    private lateinit var binding: ActivityMypageEditAccountBinding

    // 기존 회원 정보
    private var originalNickname: String? = null
    private var originalUsername: String? = null
    private var originalEmail: String? = null


    // 복원 모드 플래그 (TextWatcher가 초기화하는 것을 방지)
    private var isRestoringData = false

    // 타이머 관련 변수
    private var countDownTimer: CountDownTimer? = null
    private val TIMER_DURATION = 5 * 60 * 1000L // 5분 (밀리초)

    // EditText 유효성 상태 저장 (실시간)
    private var isNicknameValid = false
    private var isUsernameValid = false
    private var isEmailValid = false
    private var isEmailCertifyValid = false

    // 비밀번호는 조회로 데이터를 가져오지 않기 때문에 기본 true설정으로 부분 수정 가능하도록 함
    private var isPwNowValid = true
    private var isPwValid = true
    private var isPwCheckValid = true

    // 버튼 클릭 서버 통신 상태 저장
    private var isNicknameAvailable = false
    private var isUsernameAvailable = false
    private var isEmailSendSuccess = false
    private var isEmailVerified = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMypageEditAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 툴바 타이틀 변경
        setToolbarTitle("프로필 수정")
        // 툴바 뒤로가기 설정
        setBackButton()


        // 회원 정보 로드
        loadUserInfo()

        // TextWatcher 설정
        setupTextWatchers()

        // 버튼 설정
        setButtons()

        // 회원가입 저장
        updateProfile()

    }

    // 회원 정보 로드
    private fun loadUserInfo() {
        // 회원 정보 로드 API 호출
        membersService.getUserInfo()
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        // 기존 회원 정보 저장 및 입력창 텍스트 설정
                        saveAndSetUserInfo(body)
                        // 유효성 검사 UI 복원
                        setupProcess()

                        Log.d("API_DEBUG", "회원 정보 로드 성공")
                    } else {
                        Log.e("API_ERROR", "회원 정보 로드 실패")
                    }
                }
            )
    }

    // 기존 회원 정보 저장 및 입력창 텍스트 설정
    private fun saveAndSetUserInfo(body: ApiResponse<MembersUserInfoResponse>) {
        // 기존 회원 정보 저장
        originalNickname = body.data?.me?.nickname
        originalUsername = body.data?.me?.username
        originalEmail = body.data?.me?.email

        // 입력창 텍스트 복원
        binding.etNickname.setText(originalNickname)
        binding.etUsername.setText(originalUsername)
        binding.etEmail.setText(originalEmail)
    }

    // 기존 진행 상황 복원
    private fun setupProcess() {
        isRestoringData = true // 복원 시작

        // 상태 복원 (비밀번호 제외 모두 true로 설정)
        isNicknameValid = true
        isUsernameValid = true
        isEmailValid = true
        isEmailCertifyValid = true
        isNicknameAvailable = true
        isUsernameAvailable = true
        isEmailSendSuccess = true
        isEmailVerified = true

        // UI 복원
        // 닉네임
        binding.tvValidNickname.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity, R.color.green))

        // 닉네임 중복 확인
        binding.tvDuplicateNickname.text = getString(R.string.btnValid_nickname_true)
        binding.tvDuplicateNickname.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity, R.color.green))

        // 아이디
        binding.tvValidId.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity, R.color.green))

        // 아이디 중복 확인
        binding.tvDuplicateId.text = getString(R.string.btnValid_id_true)
        binding.tvDuplicateId.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity, R.color.green))


        // 이메일 코드 전송 완료
        binding.tvValidEmail.text = getString(R.string.btnValid_email_true)
        binding.tvValidEmail.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity, R.color.green))

        // 이메일 인증 완료
        binding.tvValidEmailCertify.text = getString(R.string.btnValid_email_certify_true)
        binding.tvValidEmailCertify.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity,R.color.green))

        isRestoringData = false // 복원 종료
    }

    // TextWatcher 설정 (EditText 실시간 검사)
    private fun setupTextWatchers() {
        // 닉네임
        binding.etNickname.setupValidation(
            binding.tvValidNickname,
            validator = { it.length in 4..10 }, // it : EditText에 입력된 텍스트
            onValidationChanged = { isValid ->
                isNicknameValid = isValid  // 받은 isValid를 isNicknameValid에 저장
                if (!isRestoringData) { // 복원 모드가 아닐 때만 초기화
                    isNicknameAvailable = false // 닉네임 중복 확인 후 EditText 수정 시 중복 확인 상태 초기화
                    binding.tvDuplicateNickname.text = "" // 중복 확인 메세지도 초기화
                }
            }
        )

        // 아이디
        binding.etUsername.setupValidation(
            binding.tvValidId,
            validator = { it.length in 4..16 },
            onValidationChanged = { isValid ->
                isUsernameValid = isValid
                if (!isRestoringData) {
                    isUsernameAvailable = false
                    binding.tvDuplicateId.text = ""
                }
            }
        )

        // 현재 비밀번호
        binding.etPwNow.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
            binding.tvCheckedPwNow.text = ""

            if (!text.isNullOrEmpty()) { // 입력된 경우
                isPwNowValid = false
                isPwValid = false
                isPwCheckValid = false
            } else { // 입력이 없는 경우
                isPwNowValid = true
                isPwValid = true
                isPwCheckValid = true

                // 새 비밀번호, 새 비밀번호 확인 입력창 enabled 설정
                binding.etPw.isEnabled = false
                binding.etPwCheck.isEnabled = false
            }
        }

        // 비밀번호
        binding.etPw.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
            val input = text.toString()
            binding.etPw.setBackgroundResource(R.drawable.et_input_default)
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
            if (binding.etPwCheck.text.isNullOrEmpty()) {
                binding.tvValidPwCheck.text = ""
            }
        }

        // 비밀번호 확인
        binding.etPwCheck.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
            val input = text.toString()
            val password = binding.etPw.text.toString() // 비밀번호 입력 x -> 빈 문자열 반환

            binding.etPwCheck.setBackgroundResource(R.drawable.et_input_default)

            when {
                // 비밀번호 입력 X or 비밀번호 유효성 검사 실패
                !isPwValid -> {
                    binding.tvValidPwCheck.text = getString(R.string.valid_pw_check_noPW)
                    binding.tvValidPwCheck.setTextColor(ContextCompat.getColor(this, R.color.red))
                    isPwCheckValid = false
                }
                // 비밀번호 확인 입력 X
                input.isEmpty() -> {
                    isPwCheckValid = false
                }
                // 비밀번호와 비밀번호 확인 텍스트가 일치하는 경우
                input == password -> {
                    binding.tvValidPwCheck.text = getString(R.string.valid_pw_check_true)
                    binding.tvValidPwCheck.setTextColor(ContextCompat.getColor(this, R.color.green))
                    isPwCheckValid = true
                }
                else -> {
                    binding.tvValidPwCheck.text = getString(R.string.valid_pw_check_false)
                    binding.tvValidPwCheck.setTextColor(ContextCompat.getColor(this, R.color.red))
                    isPwCheckValid = false
                }
            }
        }

        // 이메일
        binding.etEmail.setupValidation(
            binding.tvValidEmail,
            validator = { isValidEmail(it) },
            onValidationChanged = { isValid ->
                isEmailValid = isValid
                if (!isRestoringData) {
                    isEmailSendSuccess = false
                    binding.tvValidEmail.text = ""
                }
            }
        )

        // 이메일 인증번호
        binding.etEmailCertify.setupValidation(
            binding.tvValidEmailCertify,
            validator = { it.length == 6 },
            onValidationChanged = { isValid ->
                isEmailCertifyValid = isValid
                if (!isRestoringData) {
                    isEmailVerified = false
                    binding.tvValidEmailCertify.text = ""
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
        binding.tvValidPw1.setTextColor(ContextCompat.getColor(this, color1))
        binding.tvValidPw2.setTextColor(ContextCompat.getColor(this,color2))
    }

    // 버튼 설정
    private fun setButtons() {
        setupNicknameDuplicateCheck()
        setupUsernameDuplicateCheck()
        setupCurrentPasswordCheck()
        setupPwVisible()
        setupEmailSend()
        setupEmailCertify()
    }

    // 닉네임 중복 확인 버튼
    private fun setupNicknameDuplicateCheck() {
        binding.btnCheckNickname.setOnClickListener {
            // 1. 닉네임 입력값 유효성 검사
            if(!isNicknameValid) {
                // 2. 텍스트 색 변경 후 리스너 종료
                binding.tvValidNickname.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }

            // 3. 닉네임 중복 확인 API 호출 (isNicknameValid = true)
            authService.getNickname(binding.etNickname.text.toString())
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) { // 서버의 success 필드
                            binding.tvDuplicateNickname.text = getString(R.string.btnValid_nickname_true)
                            binding.tvDuplicateNickname.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity, R.color.green))
                            isNicknameAvailable = true // 중복 확인 상태 저장
                        } else { // HTTP 200인데 서버에서 실패 응답
                            binding.tvDuplicateNickname.text = body?.data ?: body?.message
                                    ?: getString(R.string.btnValid_nickname_false)
                            binding.tvDuplicateNickname.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity, R.color.red))
                            isNicknameAvailable = false
                        }
                    }
                )
        }
    }

    // 아이디 중복 확인 버튼
    private fun setupUsernameDuplicateCheck() {
        binding.btnCheckId.setOnClickListener {
            // 1. 아이디 입력값 유효성 검사
            if(!isUsernameValid) {
                // 2. 텍스트 색 변경 후 리스너 종료
                binding.tvValidId.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }

            // 3. 아이디 중복 확인 API 호출 (isNicknameValid = true)
            authService.getUsername(binding.etUsername.text.toString())
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) { // 서버의 success 필드
                            // 진짜 성공
                            binding.tvDuplicateId.text = getString(R.string.btnValid_id_true)
                            binding.tvDuplicateId.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity, R.color.green))
                            isUsernameAvailable = true // 중복 확인 상태 저장
                        } else { // HTTP 200인데 서버에서 실패 응답
                            // body?.success == false 이거나 null일 수 있음.
                            binding.tvDuplicateId.text =
                                body?.data ?: body?.message ?: getString(R.string.btnValid_id_false)
                            binding.tvDuplicateId.setTextColor(
                                ContextCompat.getColor(
                                    this@MypageEditAccountActivity,
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
        binding.btnSendEmail.setOnClickListener {
            if(!isEmailValid) { // 이메일 유효성 검사 실패
                binding.tvValidEmail.text = getString(R.string.btnValid_email_false)
                binding.tvValidEmail.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }

            // 이메일 인증번호 보내기 API 호출 (isEmailValid = true)
            // 요청 객체 생성
            val request = AuthEmailVerificationRequest(email = binding.etEmail.text.toString())
            // API 호출
            authService.requestEmailVerification(request)
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if(body.success == true) { // 진짜 성공
                            // 이메일 인증번호 보내기 성공 시 유효성 검사 텍스트 변경
                            binding.tvValidEmail.text = getString(R.string.btnValid_email_true)
                            binding.tvValidEmail.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity,R.color.green))
                            isEmailSendSuccess = true // 중복 확인 상태 저장

                            // 이메일 인증번호 타이머 시작
                            startEmailTimer()
                        } else { // HTTP 200인데 서버에서 실패 응답
                            binding.tvValidEmail.text = body.data ?: body.message ?: getString(R.string.btnValid_email_fail)
                            binding.tvValidEmail.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity,R.color.red))
                            isEmailSendSuccess = false
                        }
                    }
                )
        }
    }

    // 이메일 인증번호 인증하기 버튼
    private fun setupEmailCertify() {
        binding.btnCertifyEmail.setOnClickListener {
            // 이메일 입력 및 인증번호 전송 확인
            if(!isEmailSendSuccess) {
                binding.tvValidEmail.text = getString(R.string.btnValid_email_certify_noEmail)
                binding.tvValidEmail.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }
            // 인증번호 유효성 검사(6자리) 실패
            if(!isEmailCertifyValid) {
                binding.tvValidEmailCertify.text = getString(R.string.btnValid_email_certify_empty)
                binding.tvValidEmailCertify.setTextColor(ContextCompat.getColor(this,R.color.red))
                return@setOnClickListener
            }

            // 이메일 인증번호 인증하기 API 호출 (isEmailCertifyValid = true)
            val request = AuthVerifyEmailRequest(
                code = binding.etEmailCertify.text.toString(),
                email = binding.etEmail.text.toString()
            )
            authService.verifyEmail(request)
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if(body.success == true) { // 진짜 성공
                            binding.tvValidEmailCertify.text = getString(R.string.btnValid_email_certify_true)
                            binding.tvValidEmailCertify.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity,R.color.green))
                            isEmailVerified = true // 중복 확인 상태 저장
                        } else { // HTTP 200인데 서버에서 실패 응답
                            // body?.success == false 이거나 null일 수 있음.
                            binding.tvValidEmailCertify.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity,R.color.red))
                            isEmailVerified = false
                        }
                    },
                    onError = { _ ->
                        binding.tvValidEmailCertify.text = getString(R.string.btnValid_email_certify_false)
                        binding.tvValidEmailCertify.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity,R.color.red))
                        isEmailVerified = false
                    }
                )
        }
    }

    // 현재 비밀번호 확인 버튼
    private fun setupCurrentPasswordCheck() {
        binding.btnCheckPw.setOnClickListener {
            // 1. 현재 비밀번호 입력 확인
            if (binding.etPwNow.text.isNullOrEmpty()) { // 입력이 없는 경우
                binding.tvCheckedPwNow.text = getString(R.string.valid_pw_check_noPW)
                binding.tvCheckedPwNow.setTextColor(ContextCompat.getColor(this, R.color.red))
                return@setOnClickListener
            }
            // 2. 현재 비밀번호 검증 API 호출
            authService.verifyPassword(
                mapOf("password" to binding.etPwNow.text.toString().trim()) // "password" = String 으로 요청 전달
            ).executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        // 새 비밀번호, 새 비밀번호 검증 입력창 enabled  설정
                        binding.etPw.isEnabled = true
                        binding.etPwCheck.isEnabled = true

                        binding.etPw.setBackgroundResource(R.drawable.et_input_default)
                        binding.etPwCheck.setBackgroundResource(R.drawable.et_input_default)

                        binding.tvCheckedPwNow.text = getString(R.string.valid_pw_check_true)
                        binding.tvCheckedPwNow.setTextColor(ContextCompat.getColor(this, R.color.green))
                        isPwNowValid = true

                        Log.d("API_DEBUG", "비밀번호 검증 성공")
                    } else {
                        Log.e("API_ERROR","비밀번호 검증 실패")
                    }
                },
                onError = { errorResponse ->
                    if (errorResponse?.code == "AUTH_INVALID_PASSWORD") {
                        binding.tvCheckedPwNow.text = getString(R.string.valid_pw_check_false)
                        binding.tvCheckedPwNow.setTextColor(ContextCompat.getColor(this, R.color.red))
                        isPwNowValid = false
                    } else {
                        CustomToast.show(binding.root, "액티비티 - 서버 오류")
                    }
                }
            )
        }
    }

    // 비밀번호, 비밀번호 확인 버튼
    private fun setupPwVisible() {
        binding.ivVisiblePwNow.setupPasswordVisible(
            context = this,
            editText = binding.etPwNow
        )
        binding.ivVisiblePw.setupPasswordVisible(
            context = this,
            editText = binding.etPw
        )
        binding.ivVisiblePwCheck.setupPasswordVisible(
            context = this,
            editText = binding.etPwCheck
        )
    }

    // 이메일 인증번호 타이머
    private fun startEmailTimer() {
        // 기존 타이머가 있으면 취소
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00"
                binding.tvTimer.setTextColor(ContextCompat.getColor(this@MypageEditAccountActivity,R.color.red))
                isEmailSendSuccess = false // 인증번호 재전송 필요
            }
        }.start()
    }

    // 수정된 프로필 정보 저장
    private fun updateProfile() {
        binding.btnSavePage.setOnClickListener {
            // 유효성 검사 실패 시
            if (!validationAccount()) {
                highlightInvalidFields()
                return@setOnClickListener
            }
            // 수정된 회원 정보 찾아서 request 객체에 전달
            val request = createUpdateProfileRequest()

            // 프로필 수정 업데이트 API 호출
            membersService.updateProfile(request)
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) {
                            Log.d("API_DEBUG", "프로필 수정 성공")
                            finish()
                        } else {
                            Log.e("API_ERROR", "프로필 수정 실패")
                        }
                    }
                )
        }
    }

    // 저장 버튼 클릭 시 유효성 검사 확인
    private fun validationAccount() : Boolean {
        return isNicknameAvailable &&
                isUsernameAvailable &&
                isPwNowValid &&
                isPwValid &&
                isPwCheckValid &&
                isEmailVerified
    }

    // 유효성 검사 실페 시 입력창 배경 변경
    private fun highlightInvalidFields() {
        binding.etNickname.highlightInvalidField(isNicknameAvailable)
        binding.etUsername.highlightInvalidField(isUsernameAvailable)
        binding.etPwNow.highlightInvalidField(isPwNowValid)
        binding.etPw.highlightInvalidField(isPwValid)
        binding.etPwCheck.highlightInvalidField(isPwCheckValid)
        binding.etEmail.highlightInvalidField(isEmailVerified)
        binding.etEmailCertify.highlightInvalidField(isEmailVerified)
    }

    // 수정된 회원 정보 찾아서 request 객체에 전달
    private fun createUpdateProfileRequest() : Map<String, String> {
        val request = mutableMapOf<String, String>()

        // 현재 입력된 회원 정보
        val currentNickname = binding.etNickname.text.toString().trim() // 기존 회원 정보와 비교
        val currentUsername = binding.etUsername.text.toString().trim()
        val currentEmail = binding.etEmail.text.toString().trim()
        val currentPreviousPw = binding.etPwNow.text.toString().trim() // ""와 비교
        val currentNewPw = binding.etPw.text.toString().trim() // ""

        // 기존 회원 정보와 비교 후 수정된 항목 리퀘스트에 추가
        if (currentNickname != originalNickname) request["nickname"] = currentNickname
        if (currentUsername != originalUsername) request["username"] = currentUsername
        if (currentEmail != originalEmail) request["email"] = currentEmail
        if (currentPreviousPw.isNotEmpty()) request["currentPassword"] = currentPreviousPw
        if (currentNewPw.isNotEmpty()) request["newPassword"] = currentNewPw

        return request
    }

}