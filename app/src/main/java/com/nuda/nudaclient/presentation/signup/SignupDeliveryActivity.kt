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
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.api.RetrofitInstance
import com.nuda.nudaclient.data.remote.dto.signup.SignupDeliveryRequest
import com.nuda.nudaclient.databinding.ActivitySignupDeliveryBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.highlightInvalidField
import com.nuda.nudaclient.presentation.login.LoginActivity

class SignupDeliveryActivity : AppCompatActivity() {

    // 뷰 바인딩 객체 선언
    lateinit var binding : ActivitySignupDeliveryBinding

    // API 서비스 객체 선언
    private val signupService = RetrofitInstance.signupService

    // EditText 유효성 상태 저장 (실시간)
    private var isRecipientValid = false
    private var isPhoneNumValid = false
    private var isAddress2Valid = false

    // 버튼 클릭 서버 통신 상태 저장
    private var isAddressFindSuccess = false

    // 휴대폰번호 포맷팅 상태 저장
    private var isFormatting = false

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

        // EditText TextWatcher 설정
        setupTextWatchers()

        // 버튼 클릭 이벤트
        setupButtons()

    }

    // TextWatcher 설정 (EditText 실시간 검사)
    private fun setupTextWatchers() {
        // 수령인
        et_recipient.doAfterTextChanged { text ->
            val input = text.toString()
            when {
                input.isEmpty() -> {
                    tv_validRecipient.text = getString(R.string.valid_receiver)
                    tv_validRecipient.setTextColor(ContextCompat.getColor(this, R.color.red))
                    isRecipientValid = false
                }
                else -> {
                    tv_validRecipient.text = ""
                    tv_validRecipient.setTextColor(ContextCompat.getColor(this, R.color.green))
                    isRecipientValid = true
                }
            }
        }

        // 휴대폰번호
        et_phoneNum.doAfterTextChanged { text ->
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
            textView.setTextColor(ContextCompat.getColor(this, R.color.green))
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

            val intent = Intent(this, AddressSearchActivity::class.java)
            // 다음 우편번호 서비스 적용
            addressSearchLauncher.launch(intent)
        }
    }

    // 이전 페이지 이동 버튼
    private fun setupPrevPage() {
        binding.btnPrevPage.setOnClickListener {
            // draft 데이터 get API 추가 시 수정

            // 회원가입 1단계 페이지로 이동 (SignupAccountActivity)
            val intent = Intent(this, SignupAccountActivity::class.java)
            startActivity(intent)
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
            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    // 유효성 검사 통과 시 배송정보 데이터 업데이트
    private fun updateDeliveryData() {
        // 서버 통신 API 호출
        val signupToken = TokenManager.getSignupToken(this)
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

    // 배송 정보 입력 유효성 검사 실패 시 입력창 배경 변경
    private fun highlightInvalidFields() {
        et_recipient.highlightInvalidField(isRecipientValid)
        et_phoneNum.highlightInvalidField(isPhoneNumValid)
        et_postalCode.highlightInvalidField(isAddressFindSuccess)
        et_address1.highlightInvalidField(isAddressFindSuccess)
        et_address2.highlightInvalidField(isAddress2Valid)
    }

    // 배송정보 입력 유효성 검사
    private fun validationDelivery() : Boolean{
        return isRecipientValid &&
                isPhoneNumValid &&
                isAddressFindSuccess &&
                isAddress2Valid
    }





}