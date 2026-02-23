package com.nuda.nudaclient.presentation.mypage.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient
import com.nuda.nudaclient.data.remote.dto.common.ApiResponse
import com.nuda.nudaclient.data.remote.dto.members.MembersDeliveryInfoResponse
import com.nuda.nudaclient.data.remote.dto.signup.SignupDeliveryRequest
import com.nuda.nudaclient.databinding.ActivityMypageEditDeliveryBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.highlightInvalidField
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.signup.AddressSearchActivity

class MypageEditDeliveryActivity : BaseActivity() {

    private lateinit var binding: ActivityMypageEditDeliveryBinding

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
                binding.etZip.setText(zonecode)
                binding.etAddress.setText(address)

                // 건물명이 있으면 상세주소에 힌트로 표시
                if (buildingName.isNotEmpty()) {
                    binding.etAddressDetail.hint = buildingName
                }

                // 주소 찾기 성공 플래그
                isAddressFindSuccess = true

                // 유효성 메시지 초기화
                binding.tvValidZip.text = ""
                binding.tvValidAddress.text = ""

                // 상세 주소 초기화
                binding.etAddressDetail.setText("")

                // 상세주소 입력란에 포커스
                binding.etAddressDetail.requestFocus()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMypageEditDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setToolbar()

        // 계정 정보 로드
        loadDeliveryInfo()

        // EditText TextWatcher 설정
        setupTextWatchers()

        // 주소 찾기 버튼 설정
        setupAddressFind()

        // 배송 정보 저장
        updateDeliveryInfo()
    }


    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("배송정보 관리") // 타이틀
        setBackButton() // 뒤로가기 버튼
    }

    // 배송 정보 로드
    private fun loadDeliveryInfo() {
        RetrofitClient.membersService.getDeliveryInfo()
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        // 기존 배송 정보 입력창 텍스트로 설정
                        saveAndSetDeliveryInfo(body)
                        // 기존 진행 상황 복원
                        setupProcess()

                        Log.d("API_DEBUG", "배송정보 로드 성공")
                    } else {
                        Log.e("API_ERROR", "배송정보 로드 실패")
                    }
                }
            )
    }

    // 기본 배송 정보 입력창 텍스트로 설정
    private fun saveAndSetDeliveryInfo(body: ApiResponse<MembersDeliveryInfoResponse>) {
       binding.etRecipient.setText(body.data?.recipient)
       binding.etPhone.setText(body.data?.phoneNum)
       binding.etZip.setText(body.data?.postalCode)
       binding.etAddress.setText(body.data?.address1)
       binding.etAddressDetail.setText(body.data?.address2)
    }

    // 기존 진행상황 복원
    private fun setupProcess() {
        isRestoringData = true

        // 상태 복원
        isRecipientValid = true
        isPhoneNumValid = true
        isAddress2Valid = true
        isAddressFindSuccess = true

        // UI 복원
        // 수령인
        if (isRecipientValid) binding.tvValidReceiver.text = ""

        // 휴대폰번호
        if (isPhoneNumValid) binding.tvValidPhone.text = ""

        // 배송지
        if (isAddressFindSuccess) {
            binding.tvValidZip.text = ""
            binding.tvValidAddress.text = ""
        }

        // 배송 상세 주소
        if (isAddress2Valid) {
            binding.tvValidAddressDetail.text = ""
        }

        isRestoringData = false
    }

    // TextWatcher 설정 (EditText 실시간 검사)
    private fun setupTextWatchers() {
        // 수령인
        binding.etRecipient.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
            // 입력창 클릭 시 기본 배경 설정
            binding.etRecipient.setBackgroundResource(R.drawable.et_input_default)

            val input = text.toString()
            when {
                input.isEmpty() -> {
                    binding.tvValidReceiver.text = getString(R.string.valid_receiver)
                    binding.tvValidReceiver.setTextColor(ContextCompat.getColor(this, R.color.red))
                    isRecipientValid = false
                }
                else -> {
                    binding.tvValidReceiver.text = ""
                    isRecipientValid = true
                }
            }
        }

        // 휴대폰번호
        binding.etPhone.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
            // 입력창 클릭 시 기본 배경 설정
            binding.etPhone.setBackgroundResource(R.drawable.et_input_default)

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

            validatePhoneNumber(formatted, binding.tvValidPhone)
        }

        // 배송지
        binding.etAddressDetail.doAfterTextChanged { text ->
            if (isRestoringData) return@doAfterTextChanged
            binding.etAddressDetail.setBackgroundResource(R.drawable.et_input_default)

            val input = text.toString()
            if(!isAddressFindSuccess) { // 주소 찾기 하지 않은 경우
                setAddressValidationText(binding.tvValidZip, R.string.valid_address)
                setAddressValidationText(binding.tvValidAddress, R.string.valid_address)
                setAddressValidationText(binding.tvValidAddressDetail, R.string.valid_address)
                isAddress2Valid = false
            } else {
                when {
                    input.isEmpty() -> {
                        setAddressValidationText(binding.tvValidAddressDetail, R.string.valid_address_detail)
                        isAddress2Valid = false
                    }
                    else -> {
                        binding.tvValidAddressDetail.text = ""
                        isAddress2Valid = true
                    }
                }
            }
        }
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

    // 배송지 헬퍼 함수
    private fun setAddressValidationText(textView: TextView, text : Int) {
        textView.text = getString(text)
        textView.setTextColor(ContextCompat.getColor(this, R.color.red))
    }

    // 주소 찾기 버튼 설정
    private fun setupAddressFind() {
        binding.btnFindAddress.setOnClickListener {
            binding.etZip.setBackgroundResource(R.drawable.et_input_default)
            binding.etAddress.setBackgroundResource(R.drawable.et_input_default)

            isAddressFindSuccess = false

            // 버튼 클릭 시 입력창 기본 배경 설정
            binding.etZip.setBackgroundResource(R.drawable.et_input_default)
            binding.etAddress.setBackgroundResource(R.drawable.et_input_default)

            val intent = Intent(this, AddressSearchActivity::class.java)
            // 다음 우편번호 서비스 적용
            addressSearchLauncher.launch(intent)
        }
    }

    // 배송 정보 저장
    private fun updateDeliveryInfo() {
        binding.btnSavePage.setOnClickListener {
            // 유효성 검사 실패 시
            if(!validationDelivery()) {
                highlightInvalidFields()
                return@setOnClickListener
            }
            // 배송 정보 수정 API 호출
            RetrofitClient.membersService.updateDeliveryInfo(
                SignupDeliveryRequest(
                    address1 = binding.etAddress.text.toString().trim(),
                    address2 = binding.etAddressDetail.text.toString().trim(),
                    phoneNum = binding.etPhone.text.toString().trim(),
                    postalCode = binding.etZip.text.toString().trim(),
                    recipient = binding.etRecipient.text.toString().trim()
                )
            ).executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        finish()
                        Log.d("API_DEBUG", "배송 정보 수정 성공")
                    } else {
                        Log.e("API_ERROR", "배송 정보 수정 실패")
                    }
                }
            )
        }
    }

    private fun validationDelivery(): Boolean {
        return isRecipientValid &&
                isPhoneNumValid &&
                isAddressFindSuccess &&
                isAddress2Valid
    }

    private fun highlightInvalidFields() {
        binding.etRecipient.highlightInvalidField(isRecipientValid)
        binding.etPhone.highlightInvalidField(isPhoneNumValid)
        binding.etZip.highlightInvalidField(isAddressFindSuccess)
        binding.etAddress.highlightInvalidField(isAddressFindSuccess)
        binding.etAddressDetail.highlightInvalidField(isAddress2Valid)
    }

}