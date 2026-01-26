package com.nuda.nudaclient.presentation.signup

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.SignupDataManager
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.RetrofitClient.signupService
import com.nuda.nudaclient.data.remote.dto.signup.SignupDeliveryRequest
import com.nuda.nudaclient.databinding.ActivitySignupDeliveryBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.highlightInvalidField
import com.nuda.nudaclient.presentation.login.LoginActivity

class SignupDeliveryActivity : AppCompatActivity() {

    // 뷰 바인딩 객체 선언
    lateinit var binding : ActivitySignupDeliveryBinding

    // 회원가입 토큰 관리
    private var signupToken : String? = null
    
    // EditText 유효성 상태 저장 (실시간)
    private var isRecipientValid = false
    private var isPhoneNumValid = false
    private var isAddress2Valid = false

    // 버튼 클릭 서버 통신 상태 저장
    private var isAddressFindSuccess = false

    // 휴대폰번호 포맷팅 상태 저장
    private var isFormatting = false

    // 복원 모드 플래그 (TextWatcher가 초기화하는 것을 방지)
    private var isRestoringData = false

    // 뷰 참조
    // EditText
    private lateinit var et_recipient : EditText
    private lateinit var et_phoneNum : EditText
    private lateinit var et_postalCode : EditText
    private lateinit var et_address1 : EditText
    private lateinit var et_address2 : EditText

    // TextView
    private lateinit var tv_validRecipient : TextView
    private lateinit var tv_validPhoneNum : TextView
    private lateinit var tv_validPostalCode : TextView
    private lateinit var tv_validAddress1 : TextView
    private lateinit var tv_validAddress2 : TextView

    // ImageView - 버튼으로 사용
    private lateinit var iv_back : ImageView


    // 다음 우편번호 서비스 런처 설정
    private val addressSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val zonecode = data.getStringExtra("zonecode") ?: ""
                val address = data.getStringExtra("address") ?: ""
                val buildingName = data.getStringExtra("buildingName") ?: ""

                // EditText에 주소 데이터 채우기
                et_postalCode.setText(zonecode)
                et_address1.setText(address)

                // 건물명이 있으면 상세주소에 힌트로 표시
                if (buildingName.isNotEmpty()) {
                    et_address2.hint = buildingName
                }

                // 주소 찾기 성공 플래그
                isAddressFindSuccess = true

                // 유효성 메시지 초기화
                tv_validPostalCode.text = ""
                tv_validAddress1.text = ""

                // 상세주소 입력란에 포커스
                et_address2.requestFocus()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 뷰 바인딩 설정
        binding = ActivitySignupDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signupToken = TokenManager.getSignupToken(this)

        // 뷰 참조 초기화
        et_recipient = binding.etRecipient
        et_phoneNum = binding.etPhone
        et_postalCode = binding.etZip
        et_address1 = binding.etAddress
        et_address2 = binding.etAddressDetail

        tv_validRecipient = binding.tvValidReceiver
        tv_validPhoneNum = binding.tvValidPhone
        tv_validPostalCode = binding.tvValidZip
        tv_validAddress1 = binding.tvValidAddress
        tv_validAddress2 = binding.tvValidAddressDetail

        iv_back = binding.toolBar.ivBack

        // draft 만료 체크
        SignupDataManager.clearExpiredData(this)

        // Draft 데이터 복원
        SignupDataManager.loadPrefData(this)
        setupProcess()

        // EditText TextWatcher 설정
        setupTextWatchers()

        // 버튼 클릭 이벤트
        setupButtons()

    }

    override fun onPause() {
        super.onPause()

        saveDeliveryData()
        SignupDataManager.backupPrefData(this)
    }

    // 진행 상황 복원
    private fun setupProcess() {
        isRestoringData = true

        // 상태 복원
        isRecipientValid = SignupDataManager.isRecipientValid
        isPhoneNumValid = SignupDataManager.isPhoneNumValid
        isAddress2Valid = SignupDataManager.isAddress2Valid
        isAddressFindSuccess = SignupDataManager.isAddressFindSuccess

        // 텍스트 복원
        et_recipient.setText(SignupDataManager.recipient ?: "")
        et_phoneNum.setText(SignupDataManager.phoneNum ?: "")
        et_postalCode.setText(SignupDataManager.postalCode ?: "")
        et_address1.setText(SignupDataManager.address1 ?: "")
        et_address2.setText(SignupDataManager.address2 ?: "")

        // UI 복원
        // 수령인
        if (isRecipientValid) {
            tv_validRecipient.text = ""
        }
        // 휴대폰번호
        if (isPhoneNumValid) {
            tv_validPhoneNum.text = ""
        }

        isRestoringData = false

    }


    // TextWatcher 설정 (EditText 실시간 검사)
    private fun setupTextWatchers() {
        // 수령인
        et_recipient.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
            // 입력창 클릭 시 기본 배경 설정
            et_recipient.setBackgroundResource(R.drawable.et_input_default)

            val input = text.toString()
            when {
                input.isEmpty() -> {
                    tv_validRecipient.text = getString(R.string.valid_receiver)
                    tv_validRecipient.setTextColor(ContextCompat.getColor(this, R.color.red))
                    isRecipientValid = false
                }
                else -> {
                    tv_validRecipient.text = ""
                    isRecipientValid = true
                }
            }
        }

        // 휴대폰번호
        et_phoneNum.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
            // 입력창 클릭 시 기본 배경 설정
            et_phoneNum.setBackgroundResource(R.drawable.et_input_default)

            // 무한루프 방지
            if(isFormatting) return@doAfterTextChanged
            isFormatting = true

            // 숫자만 추출
            val digits = text.toString().replace(Regex("[^0-9]"), "")

            // 포맷팅
            val formatted = when {
                digits.length <= 3 -> digits
                digits.length <= 7 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
                digits.length <= 11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
                else -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
            }

            // 텍스트 교체
            text?.replace(0, text.length, formatted)

            isFormatting = false

            validatePhoneNumber(formatted, tv_validPhoneNum)
        }

        // 배송지
        et_address2.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
            et_address2.setBackgroundResource(R.drawable.et_input_default)

            val input = text.toString()
            if(!isAddressFindSuccess) { // 주소 찾기 하지 않은 경우
                setAddressValidationText(tv_validPostalCode, R.string.valid_address)
                setAddressValidationText(tv_validAddress1, R.string.valid_address)
                setAddressValidationText(tv_validAddress2, R.string.valid_address)
                isAddress2Valid = false
            } else {
                when {
                    input.isEmpty() -> {
                        setAddressValidationText(tv_validAddress2, R.string.valid_address_detail)
                        isAddress2Valid = false
                    }
                    else -> {
                        tv_validAddress2.text = ""
                        isAddress2Valid = true
                    }
                }
            }
        }
    }

    // 배송지 헬퍼 함수
    private fun setAddressValidationText(textView: TextView, text : Int) {
        textView.text = getString(text)
        textView.setTextColor(ContextCompat.getColor(this, R.color.red))
    }

    // 휴대폰번호 유효성 검사
    private fun validatePhoneNumber(phone: String, textView: TextView) {
    // 정확히 010-1234-5678 형식인지 체크
        val pattern = "^010-\\d{4}-\\d{4}$"

        if(phone.matches(pattern.toRegex())) {
            textView.text = ""
            isPhoneNumValid = true
        } else {
            textView.text = getString(R.string.valid_phone)
            textView.setTextColor(ContextCompat.getColor(this, R.color.red))
            isPhoneNumValid = false
        }
    }

    // 버튼 클릭 설정
    private fun setupButtons() {
        setupAddressFind()
        setupPrevPage()
        setupNextPage()
        setupBack()
    }

    // 주소 찾기 버튼
    private fun setupAddressFind() {
        binding.btnFindAddress.setOnClickListener {
            isAddressFindSuccess = false

            // 버튼 클릭 시 입력창 기본 배경 설정
            et_postalCode.setBackgroundResource(R.drawable.et_input_default)
            et_address1.setBackgroundResource(R.drawable.et_input_default)

            val intent = Intent(this, AddressSearchActivity::class.java)
            // 다음 우편번호 서비스 적용
            addressSearchLauncher.launch(intent)
        }
    }

    // 이전 페이지 이동 버튼
    private fun setupPrevPage() {
        binding.btnPrevPage.setOnClickListener {
            // 현재 작성 중인 데이터 pref에 백업
            saveDeliveryData()
            SignupDataManager.backupPrefData(this)

            finish() // 현재 액티비티 종료
        }
    }

    // 다음 페이지 이동 버튼
    private fun setupNextPage() {
        binding.btnNextPage.setOnClickListener {
            if(!validationDelivery()) {
                highlightInvalidFields()
                return@setOnClickListener
            }
            updateDeliveryData()
        }
    }


    // 뒤로가기 버튼
    private fun setupBack() {
        iv_back.setOnClickListener {
            // 작성 배송 정보 및 상태 데이터 저장
            saveDeliveryData()
            SignupDataManager.backupPrefData(this)

            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    // 배송정보 입력 유효성 검사
    private fun validationDelivery() : Boolean{
        return isRecipientValid &&
                isPhoneNumValid &&
                isAddressFindSuccess &&
                isAddress2Valid
    }

    // 배송 정보 입력 유효성 검사 실패 시 입력창 배경 변경
    private fun highlightInvalidFields() {
        et_recipient.highlightInvalidField(isRecipientValid)
        et_phoneNum.highlightInvalidField(isPhoneNumValid)
        et_postalCode.highlightInvalidField(isAddressFindSuccess)
        et_address1.highlightInvalidField(isAddressFindSuccess)
        et_address2.highlightInvalidField(isAddress2Valid)
    }

    // 유효성 검사 통과 시 배송정보 데이터 업데이트
    private fun updateDeliveryData() {
        // 서버 통신 API 호출
        val signupDeliveryRequest = SignupDeliveryRequest(
            address1 = et_address1.text.toString().trim(),
            address2 = et_address2.text.toString().trim(),
            phoneNum = et_phoneNum.text.toString().trim(),
            postalCode = et_postalCode.text.toString().trim(),
            recipient = et_recipient.text.toString().trim()
        )
        signupService.updateDelivery(signupToken, signupDeliveryRequest)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        // 임시 토스트 메세지
                        Toast.makeText(this, body.data, Toast.LENGTH_LONG).show()
                        // 최신 draft 조회 및 pref 백업
                        backupDelivery()
                        // 회원가입 3단계 페이지로 이동 (SignupSurveyActivity)
                        val intent = Intent(this, SignupSurveyActivity::class.java)
                        startActivity(intent)
                    } else {
                        // 임시 토스트 메세지
                        Toast.makeText(this, body.message, Toast.LENGTH_LONG).show()
                    }
                }
            )
    }


    // 최신 draft 조회 및 pref 백업
    private fun backupDelivery() {
        // 업데이트 된 최신 draft 조회
        signupService.getDraft(signupToken)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        // 갱신된 draft 유효기간 저장
                        SignupDataManager.expiresAt =  body.data.expiresAt
                        // 배송 정보 및 유효성 상태 데이터 싱글턴 변수에 저장
                        saveDeliveryData()
                        // pref에 전체 백업
                        SignupDataManager.backupPrefData(this)
                    }
                }
            )
    }

    // 배송 정보 및 유효성 검사 상태 데이터 저장
    private fun saveDeliveryData() {
        // 입력한 배송 정보
        SignupDataManager.recipient = et_recipient.text.toString().trim()
        SignupDataManager.phoneNum = et_phoneNum.text.toString().trim()
        SignupDataManager.postalCode = et_postalCode.text.toString().trim()
        SignupDataManager.address1 = et_address1.text.toString().trim()
        SignupDataManager.address2 = et_address2.text.toString().trim()

        // 유효성 검사 상태
        SignupDataManager.isRecipientValid = isRecipientValid
        SignupDataManager.isPhoneNumValid = isPhoneNumValid
        SignupDataManager.isAddress2Valid = isAddress2Valid
        SignupDataManager.isAddressFindSuccess = isAddressFindSuccess
    }




}