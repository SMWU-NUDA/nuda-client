package com.nuda.nudaclient.presentation.signup

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.api.RetrofitInstance
import com.nuda.nudaclient.data.remote.dto.signup.SignupSurveyRequest
import com.nuda.nudaclient.databinding.ActivitySignupSurveyBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.login.LoginActivity
import com.nuda.nudaclient.utils.CustomToast

class SignupSurveyActivity : AppCompatActivity() {

    // 뷰 바인딩 객체 선언
    lateinit var binding : ActivitySignupSurveyBinding

    private val signupService = RetrofitInstance.signupService

    // 변수 선언
    private lateinit var answerIrritationLevel : String
    private lateinit var answerScent : String
    private lateinit var answerChangeFrequency : String
    private lateinit var answerThickness : String
    private lateinit var answerPriority : String
    private var productIds : List<Int> = emptyList()

    // 뷰 참조 선언
    // RadioGroup
    private lateinit var rg_sensitivity : RadioGroup
    private lateinit var rg_scent : RadioGroup
    private lateinit var rg_flow : RadioGroup
    private lateinit var rg_thickness : RadioGroup
    private lateinit var rg_priority : RadioGroup

    private lateinit var iv_back : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 뷰 바인딩 설정
        binding = ActivitySignupSurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 뷰 참조 초기화
        rg_sensitivity = binding.radioAnswerSensitivity
        rg_scent = binding.radioAnswerScent
        rg_flow = binding.radioAnswerFlow
        rg_thickness = binding.radioAnswerThickness
        rg_priority = binding.radioAnswerPriority

        iv_back = binding.ivBack

        // 버튼 클릭 이벤트 설정
        setupButtons()
        
    }

    // 설문 응답 값 서버 전달 문자열에 저장
    private fun getSurveyResults() {

        // 민감도
        answerIrritationLevel = when (getSurveyAnswer(rg_sensitivity)) {
            "LOW" -> "NONE"
            "MIDDLE" -> "SOMETIMES"
            "HIGH" -> "OFTEN"
            else -> "NULL"
        }

        // 향
        answerScent = when (getSurveyAnswer(rg_scent)) {
            "LOW" -> "NONE"
            "MIDDLE" -> "MILD"
            "HIGH" -> "STRONG"
            else -> "NULL"
        }

        // 교체 주기(양)
        answerChangeFrequency = when (getSurveyAnswer(rg_flow)) {
            "LOW" -> "LOW"
            "MIDDLE" -> "MEDIUM"
            "HIGH" -> "HIGH"
            else -> "NULL"
        }

        // 두께
        answerThickness = when (getSurveyAnswer(rg_thickness)) {
            "LOW" -> "THIN"
            "MIDDLE" -> "NORMAL"
            "HIGH" -> "THICK"
            else -> "NULL"
        }

        // 우선순위
        answerPriority = when (getSurveyAnswer(rg_priority)) {
            "LOW" -> "SAFETY"
            "MIDDLE" -> "ABSORPTION"
            "HIGH" -> "SOFTNESS"
            else -> "NULL"
        }
    }

    // 설문 라디오그룹의 선택 라디오버튼 값 가져오기
    private fun getSurveyAnswer(
        radioGroup: RadioGroup) : String {
        val selectedBtnId = radioGroup.checkedRadioButtonId
        val selectedBtnTag : String

        // 선택된 버튼이 없을 때
        if(selectedBtnId == -1) return "NULL"
        // 선택된 버튼이 있을 때
        return radioGroup.findViewById<RadioButton>(selectedBtnId).tag.toString()
    }

    // 버튼 클릭 설정
    private fun setupButtons() {
        setupPrevPage()
        setupRegister()
        setupBack()
    }


    // 이전 페이지 이동 버튼
    private fun setupPrevPage() {
        binding.btnPrevPage.setOnClickListener {
            // 회원가입 2단계 페이지로 이동 (SignupDeliveryActivity)
            val intent = Intent(this, SignupDeliveryActivity::class.java)
            startActivity(intent)
        }
    }

    // 회원가입 버튼
    private fun setupRegister() {
        binding.btnRegister.setOnClickListener {
            // 설문 조사 데이터 수집
            getSurveyResults()

            // 유효성 검사
            if(validationSurvey()) {
                CustomToast.show(binding.root, "모든 항목에 응답해 주세요")
                return@setOnClickListener
            }
            // draft에 데이터 업데이트
            updateSurveyData()

            // 회원가입 API 호출
            signupCommit()

            // 로그인 페이지로 이동 (LoginActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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

    // 설문 유효성 검사
    private fun validationSurvey() : Boolean {
        // 하나라도 미응답 시
        return answerIrritationLevel == "NULL" ||
                answerScent == "NULL" ||
                answerChangeFrequency == "NULL" ||
                answerThickness == "NULL" ||
                answerPriority == "NULL" ||
                productIds.isEmpty()
    }

    // 유효성 검사 통과 시 설문 결과 데이터 업데이트
    private fun updateSurveyData() {
        val signupToken = TokenManager.getSignupToken(this)
        val SignupSurveyRequest = SignupSurveyRequest(
            changeFrequency = answerChangeFrequency,
            irritationLevel = answerIrritationLevel,
            priority = answerPriority,
            scent = answerScent,
            thickness = answerThickness,
            productIds = productIds
        )

        // 설문 데이터 draft에 업데이트 API 호출
        signupService.updateSurvey(signupToken, SignupSurveyRequest)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        // 임시 토스트 메세지
                        CustomToast.show(binding.root, body.data)
                    } else {
                        CustomToast.show(binding.root, body.message)
                    }
                }
            )

    }

    // 회원가입 완료
    private fun signupCommit() {
        val signupToken = TokenManager.getSignupToken(this)

        signupService.createSignup(signupToken)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        CustomToast.show(binding.root, body.data)
                    } else {
                        CustomToast.show(binding.root, body.message)
                    }
                }
            )
    }

}

