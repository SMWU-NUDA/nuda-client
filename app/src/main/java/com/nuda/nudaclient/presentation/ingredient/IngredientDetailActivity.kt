package com.nuda.nudaclient.presentation.ingredient

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.ingredientsService
import com.nuda.nudaclient.databinding.ActivityIngredientDetailBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import org.w3c.dom.Text

class IngredientDetailActivity : BaseActivity() {
    // 성분 상세페이지 진입 시 intent에 ingredientId 함께 전달 필요!!

    // TODO h코드 위험도 색상 추가 필요

    private lateinit var binding: ActivityIngredientDetailBinding

    private var ingredientId: Int = -1

    // 버튼 클릭 시 변경할 폰트 저장
    // lazy로 지연 초기화 (변수가 처음 사용될 때 초기화)
    private val typefaceUnclicked by lazy {ResourcesCompat.getFont(this, R.font.pretendard_medium)}
    private val typefaceClicked by lazy { ResourcesCompat.getFont(this, R.font.pretendard_bold)}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityIngredientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Intent에서 ingredientId 받기
        ingredientId = intent.getIntExtra("INGREDIENT_ID", -1)

        setToolbar()

        loadIngredientInfo() // 성분 정보 로드

        setIngredientWishButtons() // 성분 즐겨찾기 버튼 설정

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("성분 정보") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarShadow(false) // 그림자 숨김
    }

    // 성분 상세 정보 로드
    private fun loadIngredientInfo() {
        ingredientsService.getIngredientDetail(ingredientId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            // 텍스트 UI 업데이트
                            binding.tvIngredientName.text = data.name
                            binding.tvComponent.text = data.layerType
                            binding.tvIngredientInfo.text = data.description
                            binding.tvComponent2.text = data.name

                            // 구성요소 설명
                            binding.tvComponentInfo.text = when (data.layerType) {
                                "TOP_SHEET" -> ContextCompat.getString(this,R.string.component_coverLayerInfo)
                                "ABSORBER" -> ContextCompat.getString(this,R.string.component_absorbentCoreInfo)
                                "BACK_SHEET" -> ContextCompat.getString(this,R.string.component_waterproofLayerInfo)
                                "ADHESIVE" -> ContextCompat.getString(this,R.string.component_adhesiveInfo)
                                "ADDITIVE" -> ContextCompat.getString(this,R.string.component_additionalPartsInfo)
                                else -> "구성요소가 없습니다"
                            }

                            // 주의사항 처리 (H코드 없으면 주의사항도 없음)
                            if (data.caution == null) binding.llCaution.visibility = View.GONE // 주의사항 항목 gone 처리
                            else binding.tvCautionInfo.text = data.caution

                            // H코드 목록
                            binding.llHcodeList.removeAllViews() // 중복 방지
                            data.hcodes.forEach { hcode ->
                                val itemView = LayoutInflater.from(this).inflate(
                                    R.layout.item_hcode, // H코드 아이템 xml
                                    binding.llHcodeList, // 아이템을 추가할 LinearLayout
                                    false)
                                if (hcode.description == "") {
                                    itemView.findViewById<TextView>(R.id.tv_Hcode).text = "H코드 없음(안전)"
                                    itemView.findViewById<TextView>(R.id.tv_Hcode_info).visibility = View.GONE // H코드 설명 gone
                                } else {
                                    itemView.findViewById<TextView>(R.id.tv_Hcode).text = hcode.code
                                    // TODO h코드 위험도 색상 추가 필요
                                    itemView.findViewById<TextView>(R.id.tv_Hcode_info).text = hcode.description
                                }

                                binding.llHcodeList.addView(itemView) // LinearLayout에 아이템 추가
                            }

                            // 성분 즐겨찾기 버튼 로드
                            when (data.preference) {
                                true -> { // 관심
                                    resetButtons()
                                    binding.btnHighlight.isSelected = true
                                    binding.btnHighlight.typeface = typefaceClicked
                                }
                                false -> { // 피하기
                                    resetButtons()
                                    binding.btnAvoid.isSelected = true
                                    binding.btnAvoid.typeface = typefaceClicked
                                }
                                null -> { // 둘 다 선택 x
                                    resetButtons()
                                }
                            }
                        }
                    }
                }
            )
    }


    // 버튼 상태 초기화 함수
    private fun resetButtons() {
        // 버튼 선택 상태 초기화
        binding.btnHighlight.isSelected = false
        binding.btnAvoid.isSelected = false
        // 폰트 초기화
        binding.btnHighlight.typeface = typefaceUnclicked
        binding.btnAvoid.typeface = typefaceUnclicked
    }

    // 성분 즐겨찾기 버튼 설정
    private fun setIngredientWishButtons() {
        val btnHighlight = binding.btnHighlight
        val btnAvoid = binding.btnAvoid

        // 관심 버튼 클릭
        btnHighlight.setOnClickListener {
            ingredientsService.createIngredientLike(
                ingredientId = ingredientId,
                preference = true
            ).executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            when(data.preference) {
                                true -> { // 관심 등록
                                    resetButtons() // 다른 버튼 해제
                                    btnHighlight.isSelected = true
                                    btnHighlight.typeface = typefaceClicked // 버튼 폰트 변경
                                    Log.d("API_DEBUG", "성분 관심 등록 성공")
                                }
                                null -> { // 관심 취소
                                    resetButtons() // 둘 다 해제
                                    btnHighlight.typeface = typefaceUnclicked
                                    Log.d("API_DEBUG", "성분 관심 등록 취소")
                                }
                                else -> Log.d("API_DEBUG", "성분 관심 등록 실패")
                            }
                        }
                    }
                }
            )
        }
        // 피하기 버튼 클릭
        btnAvoid.setOnClickListener {
            ingredientsService.createIngredientLike(
                ingredientId = ingredientId,
                preference = false
            ).executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            when(data.preference) {
                                true -> { // 피하기 등록
                                    resetButtons() // 다른 버튼 해제
                                    btnAvoid.isSelected = true
                                    btnAvoid.typeface = typefaceClicked // 버튼 폰트 변경
                                    Log.d("API_DEBUG", "성분 피하기 등록 성공")
                                }
                                null -> { // 피하기 취소
                                    resetButtons() // 둘 다 해제
                                    btnAvoid.typeface = typefaceUnclicked
                                    Log.d("API_DEBUG", "성분 피하기 등록 취소")
                                }
                                else -> Log.d("API_DEBUG", "성분 피하기 등록 실패")
                            }
                        }
                    }
                }
            )
        }
    }
}