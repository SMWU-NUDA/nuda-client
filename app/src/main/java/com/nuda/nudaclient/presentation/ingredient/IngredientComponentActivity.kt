package com.nuda.nudaclient.presentation.ingredient

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.ingredientsService
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetAllResponse
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetSummaryResponse
import com.nuda.nudaclient.databinding.ActivityIngredientComponentBinding
import com.nuda.nudaclient.databinding.ActivityIngredientDetailBinding
import com.nuda.nudaclient.databinding.ActivityReviewAllBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.common.fragment.SortBottomSheet
import com.nuda.nudaclient.presentation.ingredient.adapter.IngredientItemAdapter
import com.nuda.nudaclient.utils.setupBarGraph

class IngredientComponentActivity : BaseActivity() {

    // 상품 구성 성분 화면으로 이동할 때 Intent에 productId 담아서 전달 필요 !!!

    // TODO 상품 성분 구성 요약 정보 로드
    // TODO 성분 아이템 클릭 이벤트 로직 추가 : 어댑터 설정에 추가 (ingredientId Intent에 함께 전달)
    // TODO BottomSheetDialog 추가 (다른 화면거 복붙해도 ㄱㅊ)

    private lateinit var binding: ActivityIngredientComponentBinding

    private var productId: Int = -1
    private var selectedSortTypeIdx = 0

    private lateinit var ingredientAdapter: IngredientItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityIngredientComponentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Intent에서 productId 받기
        productId = intent.getIntExtra("PRODUCT_ID", -1)

        setToolbar() // 툴바 설정

        loadIngredientInfo() // 구성 성분 정보 로드
        setFilterButton() // 필터링 버튼 설정
        
        loadIngredientItems("ALL") // 전성분 아이템 목록 로드

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("성분") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarShadow(false) // 그림자 숨김
    }

    // 성분 정보 로드
    private fun loadIngredientInfo() {
        ingredientsService.getIngredientSummary(productId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        updateIngredientUI(body.data)
                    }
                }
            )
    }

    // 성분 정보 UI 업데이트
    private fun updateIngredientUI(data: IngredientsGetSummaryResponse?) {
        data?.let { data ->
            // 막대그래프 구성
            setupBarGraph( // 표지
                binding.llTopSheet,
                data.ingredientCounts.topSheet.count,
                data.ingredientCounts.topSheet.riskCounts)
            setupBarGraph( // 흡수체
                binding.llAbsorber,
                data.ingredientCounts.absorber.count,
                data.ingredientCounts.absorber.riskCounts)
            setupBarGraph( // 방수층
                binding.llBackSheet,
                data.ingredientCounts.backSheet.count,
                data.ingredientCounts.backSheet.riskCounts)
            setupBarGraph( // 접착제
                binding.llAdhesive,
                data.ingredientCounts.adhesive.count,
                data.ingredientCounts.adhesive.riskCounts)
            setupBarGraph( // 기타
                binding.llAdditive,
                data.ingredientCounts.additive.count,
                data.ingredientCounts.additive.riskCounts)

            // 구성요소 별 성분 개수 텍스트
            binding.tvTopSheetCount.text = "${data.ingredientCounts.topSheet.count}개"
            binding.tvAbsorberCount.text = "${data.ingredientCounts.absorber.count}개"
            binding.tvBackSheetCount.text = "${data.ingredientCounts.backSheet.count}개"
            binding.tvAdhesiveCount.text = "${data.ingredientCounts.adhesive.count}개"
            binding.tvAdditiveCount.text = "${data.ingredientCounts.additive.count}개"

            // 항목 별 개수 텍스트
            binding.tvIngredientsAllCount.text = "${data.totalCount}개"
            binding.tvIngredientsCautionCount.text = "${data.globalRiskCounts.warn}개"
            binding.tvIngredientsDangerCount.text = "${data.globalRiskCounts.danger}개"
            binding.tvIngredientsHighlightCount.text = "${data.myIngredientCounts.prefer}개"
        }
    }

    // 성분 아이템 목록 로드
    private fun loadIngredientItems(filter: String) {
        // 어댑터 연결 및 API 연동
        ingredientsService.getAllIngredients(
            productId = productId,
            filter = filter
        ).executeWithHandler(
            context = this,
            onSuccess = { body ->
                if (body.success == true) {
                    body.data?.let { data ->
                        binding.tvTotalCount.text = "${data.totalCount.toString()}개"
                        setupIngredientAdapter(data.ingredients)
                    }
                }
            }
        )
    }

    // 성분 목록 어댑터 설정
    private fun setupIngredientAdapter(ingredientList: List<IngredientsGetAllResponse.Ingredient>) {
        // 어댑터 설정
        ingredientAdapter = IngredientItemAdapter(
            ingredientList = ingredientList,
            onItemClick = { ingredientId -> // 성분 아이템 클릭 이벤트 설정
                // 성분 상세페이지로 이동, ingredientId 전달
                val intent = Intent(this, IngredientDetailActivity::class.java)
                intent.putExtra("INGREDIENT_ID", ingredientId)
                startActivity(intent)
            })
        binding.rvIngredientList.apply {
            adapter = ingredientAdapter // 리사이클러뷰에 어댑터 연결
            layoutManager = LinearLayoutManager(this@IngredientComponentActivity)
        }
    }

    // 필터링 버튼 클릭 이벤트 -> BottomSheetDialog 프래그먼트 호출
    private fun setFilterButton() {
        val BtnFilter = binding.btnFilter
        val tvFilter = binding.tvIngredientFilter

        BtnFilter.setOnClickListener {
            SortBottomSheet.newInstance(
                options = listOf("전성분", "주의 성분", "위험 성분", "관심 성분", "피할 성분"),
                sortTypes = listOf("DEFAULT", "WARN", "DANGER", "HIGHLIGHT", "AVOID"),
                selectedIndex = selectedSortTypeIdx
            ){ sortType ->
                when (sortType) {
                    "DEFAULT" -> {
                        selectedSortTypeIdx = 0
                        BtnFilter.text = "전성분"
                        tvFilter.text = "전성분"
                        loadIngredientItems("ALL")
                    }
                    "WARN" -> {
                        selectedSortTypeIdx = 1
                        BtnFilter.text = "주의 성분"
                        tvFilter.text = "주의 성분"
                        loadIngredientItems("WARN") }
                    "DANGER" -> {
                        selectedSortTypeIdx = 2
                        BtnFilter.text = "위험 성분"
                        tvFilter.text = "위험 성분"
                        loadIngredientItems("DANGER") }
                    "HIGHLIGHT" -> {
                        selectedSortTypeIdx = 3
                        BtnFilter.text = "관심 성분"
                        tvFilter.text = "관심 성분"
                        loadIngredientItems("INTEREST")
                    }
                    "AVOID" -> {
                        selectedSortTypeIdx = 4
                        BtnFilter.text = "피할 성분"
                        tvFilter.text = "피할 성분"
                        loadIngredientItems("AVOID")
                    }
                }
            }.show(supportFragmentManager, "SortBottomSheet")

        }
    }




}