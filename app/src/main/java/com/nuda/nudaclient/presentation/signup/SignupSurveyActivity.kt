package com.nuda.nudaclient.presentation.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.local.SignupDataManager
import com.nuda.nudaclient.data.local.TokenManager
import com.nuda.nudaclient.data.remote.RetrofitClient.signupService
import com.nuda.nudaclient.data.remote.dto.signup.SignupSurveyRequest
import com.nuda.nudaclient.databinding.ActivitySignupSurveyBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.login.LoginActivity
import com.nuda.nudaclient.presentation.search.SearchResultActivity
import com.nuda.nudaclient.utils.CustomToast

class SignupSurveyActivity : AppCompatActivity() {

    private val TAG = "SignupSurveyActivity"

    // 뷰 바인딩 객체 선언
    lateinit var binding : ActivitySignupSurveyBinding

    var signupToken : String? = null

    // 복원 모드 플래그 (TextWatcher가 초기화하는 것을 방지)
    private var isRestoringData = false

    // 변수 선언
    private var answerIrritationLevel = "NULL"
    private var answerScent = "NULL"
    private var answerChangeFrequency = "NULL"
    private var answerThickness = "NULL"
    private var answerAdhesion = "NULL"

    // 뷰 참조 선언
    // RadioGroup
    private lateinit var rg_sensitivity : RadioGroup
    private lateinit var rg_scent : RadioGroup
    private lateinit var rg_flow : RadioGroup
    private lateinit var rg_thickness : RadioGroup
    private lateinit var rg_adhesion : RadioGroup

    private lateinit var iv_back : ImageView

    // 선택한 사용 제품 아이디 리스트
    private var selectedProductIds = mutableListOf<Int>()
    // 선택한 사용 제품 썸네일 리스트
    private var selectedThumnails = mutableListOf<String>()

    // launcher 등록 (onCreate 전에 선언)
    private val  searchProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val productId = result.data?.getIntExtra("PRODUCT_ID", -1)
            val thumbnail = result.data?.getStringExtra("PRODUCT_THUMBNAIL")

            if (productId == -1) return@registerForActivityResult

            // 중복 체크 후 추가
            if (selectedProductIds.none { it == productId }) {
                selectedProductIds.add(productId ?: -1) // 상품 아이디 리스트에 추가
                selectedThumnails.add(thumbnail ?: "") // 썸네일 리스트에 추가
                updateProductThumbnail(selectedThumnails) // 썸네일 리스트 업데이트
            }
            Log.d("API_DEBUG", "[$TAG] 사용 상품 추가 완료")
        }
    }

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

        signupToken = TokenManager.getSignupToken(this)

        // 뷰 참조 초기화
        rg_sensitivity = binding.radioAnswerSensitivity
        rg_scent = binding.radioAnswerScent
        rg_flow = binding.radioAnswerFlow
        rg_thickness = binding.radioAnswerThickness
        rg_adhesion = binding.radioAnswerAdhesion

        iv_back = binding.ivBack

        // draft 만료 체크
        SignupDataManager.clearExpiredData(this)

        // Draft 데이터 복원
        SignupDataManager.loadPrefData(this)
        setupProcess()

        // 버튼 클릭 이벤트 설정
        setupButtons()
        
    }

    override fun onPause() {
        super.onPause()

        getSurveyResults()
        saveSurveyData()
        SignupDataManager.backupPrefData(this)
    }

    // 진행 상황 복원
    private fun setupProcess() {
        isRestoringData = true

        // 설문 조사 상태 복원
        answerIrritationLevel = SignupDataManager.irritationLevel ?: "NULL"
        answerScent = SignupDataManager.scent ?: "NULL"
        answerChangeFrequency = SignupDataManager.changeFrequency ?: "NULL"
        answerThickness = SignupDataManager.thickness ?: "NULL"
        answerAdhesion = SignupDataManager.adhesion ?: "NULL"
        selectedProductIds = SignupDataManager.productIds as MutableList<Int>

        // UI 복원
        if(answerIrritationLevel != "NULL") {
            val tagValue = convertIrritationLevel(answerIrritationLevel)
            selectedRadioButtonByTag(rg_sensitivity, tagValue)
        }
        if(answerScent != "NULL") {
            val tagValue = convertScent(answerScent)
            selectedRadioButtonByTag(rg_scent, tagValue)
        }
        if(answerChangeFrequency != "NULL") {
            val tagValue = convertChangeFrequency(answerChangeFrequency)
            selectedRadioButtonByTag(rg_flow, tagValue)
        }
        if(answerThickness != "NULL") {
            val tagValue = convertThickness(answerThickness)
            selectedRadioButtonByTag(rg_thickness, tagValue)
        }
        if(answerAdhesion != "NULL") {
            val tagValue = convertAdhesion(answerAdhesion)
            selectedRadioButtonByTag(rg_adhesion, tagValue)
        }

        isRestoringData = false
    }

    // 태그로 라디오버튼 선택
    private fun selectedRadioButtonByTag(radioGroup: RadioGroup, tagValue: String) {
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            if (radioButton.tag == tagValue) {
                radioButton.isChecked = true
                return
            }
        }
    }

    // 서버 값 -> 태그 값 변환 함수들
    private fun convertIrritationLevel(serverValue: String) : String {
        return when (serverValue) {
            "NONE" -> "LOW"
            "SOMETIMES" -> "MIDDLE"
            "OFTEN" -> "HIGH"
            else -> "NULL"
        }
    }

    private fun convertScent(serverValue: String) : String {
        return when (serverValue) {
            "NONE" -> "LOW"
            "MILD" -> "MIDDLE"
            "STRONG" -> "HIGH"
            else -> "NULL"
        }
    }

    private fun convertChangeFrequency(serverValue: String) : String {
        return when (serverValue) {
            "LOW" -> "LOW"
            "MEDIUM" -> "MIDDLE"
            "HIGH" -> "HIGH"
            else -> "NULL"
        }
    }

    private fun convertThickness(serverValue: String) : String {
        return when (serverValue) {
            "THIN" -> "LOW"
            "NORMAL" -> "MIDDLE"
            "THICK" -> "HIGH"
            else -> "NULL"
        }
    }

    private fun convertAdhesion(serverValue: String) : String {
        return when (serverValue) {
            "WEAK" -> "LOW"
            "NORMAL" -> "MIDDLE"
            "STRONG" -> "HIGH"
            else -> "NULL"
        }
    }

    // 설문 응답 값을 서버에 전달힐 문자열에 저장
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

        // 접착력
        answerAdhesion = when (getSurveyAnswer(rg_adhesion)) {
            "LOW" -> "WEAK"
            "MIDDLE" -> "NORMAL"
            "HIGH" -> "STRONG"
            else -> "NULL"
        }
    }

    // 설문 라디오그룹의 선택 라디오버튼 값 가져오기
    private fun getSurveyAnswer(
        radioGroup: RadioGroup) : String {
        val selectedBtnId = radioGroup.checkedRadioButtonId

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
        setSearchUsedProduct()
    }

    // 사용한 제품 선택 버튼
    private fun setSearchUsedProduct() {
        binding.btnAddProduct.setOnClickListener {
            val intent = Intent(this, SearchResultActivity::class.java)
            intent.putExtra("PAGEMODE", "PRODUCT_SIGNUP")
            searchProductLauncher.launch(intent)

            Log.d("API_DEBUG", "[$TAG] 사용 상품 검색으로 이동")
        }
    }

    // 썸네일 이미지 동적 추가
    private fun updateProductThumbnail(thumbnails: List<String>) {
        val container = binding.llImageContainer
        container.removeAllViews() // 기존 뷰 제거 후 다시 그림

        // 선택된 이미지마다 아이템 뷰 추가
        thumbnails.forEachIndexed { index, thumbnail ->
            val itemView = layoutInflater.inflate(R.layout.item_review_upload_image, container, false)
            val ivPreview = itemView.findViewById<ImageView>(R.id.ivPreview)
            val btnDelete = itemView.findViewById<ImageView>(R.id.btnDelete)

            // 이미지 로드 (Glide 사용)
            Glide.with(this).load(thumbnail).into(ivPreview)

            // x 버튼 클릭 시 해당 이미지 삭제
            btnDelete.setOnClickListener {
                selectedProductIds.removeAt(index) // 상품 아이디 리스트에서 삭제
                selectedThumnails.removeAt(index) // 썸네일 리스트에서 삭제
                updateProductThumbnail(selectedThumnails) // 미리보기 갱신
            }

            container.addView(itemView) // 화면 이미지 리스트에 추가
        }
    }

    // 이전 페이지 이동 버튼
    private fun setupPrevPage() {
        binding.btnPrevPage.setOnClickListener {
            getSurveyResults()

            saveSurveyData()

            SignupDataManager.backupPrefData(this)

            finish() // 현재 액티비티 종료
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
            // draft에 데이터 업데이트 및 회원가입 진행
            updateSurveyData()
        }
    }

    // 뒤로가기 버튼
    private fun setupBack() {
        iv_back.setOnClickListener {
            getSurveyResults()
            saveSurveyData()
            SignupDataManager.backupPrefData(this)

            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // 설문 유효성 검사
    private fun validationSurvey() : Boolean {
        // 하나라도 미응답 시
        return answerIrritationLevel == "NULL" ||
                answerScent == "NULL" ||
                answerChangeFrequency == "NULL" ||
                answerThickness == "NULL" ||
                answerAdhesion == "NULL" ||
                selectedProductIds.isEmpty()
    }

    // 유효성 검사 통과 시 설문 결과 데이터 업데이트
    private fun updateSurveyData() {
        val SignupSurveyRequest = SignupSurveyRequest(
            changeFrequency = answerChangeFrequency,
            irritationLevel = answerIrritationLevel,
            adhesion = answerAdhesion,
            scent = answerScent,
            thickness = answerThickness,
            productIds = selectedProductIds
        )

        // 설문 데이터 draft에 업데이트 API 호출
        signupService.updateSurvey(signupToken, SignupSurveyRequest)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        // 최신 draft 조회 및 pref 백업
                        backupSurvey()
                        // 회원가입 API 호출
                        signupCommit()
                    }
                }
            )

    }

    // 회원가입 완료
    private fun signupCommit() {
        signupService.createSignup(signupToken)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        Log.d("API_DEBUG", "[$TAG] 회원가입 성공, 로그인으로 이동")

                        // 회원가입 데이터 pref 삭제 clear
                        SignupDataManager.clearAllData(this)
                        // 로그인 페이지로 이동 (LoginActivity)
                        val intent = Intent(this, LoginActivity::class.java)
                        // 모든 회원가입 액티비티 종료
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra("SHOW_TOAST", "회원가입되었습니다")
                        startActivity(intent)
                        finish()
                    }
                }
            )
    }

    // 최신 draft 조회 및 pref 백업
    private fun backupSurvey() {
        // 업데이트 된 최신 draft 조회
        signupService.getDraft(signupToken)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if(body.success == true) {
                        // 갱신된 draft 유효기간 저장
                        SignupDataManager.expiresAt =  body.data?.expiresAt
                        // 입력한 설문 정보, 유효성 검사 상태 데이터 싱글턴 변수 저장
                        saveSurveyData()
                        // pref 백업
                        SignupDataManager.backupPrefData(this)
                    }
                }
            )
    }

    private fun saveSurveyData() {
        // 입력한 설문 정보
        SignupDataManager.irritationLevel = answerIrritationLevel
        SignupDataManager.scent = answerScent
        SignupDataManager.changeFrequency = answerChangeFrequency
        SignupDataManager.thickness = answerThickness
        SignupDataManager.adhesion = answerAdhesion
        SignupDataManager.productIds = selectedProductIds
    }
}

