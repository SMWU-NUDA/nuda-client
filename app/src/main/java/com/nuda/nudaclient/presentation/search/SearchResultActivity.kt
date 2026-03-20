package com.nuda.nudaclient.presentation.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivitySearchResultBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.ingredient.IngredientDetailActivity
import com.nuda.nudaclient.presentation.ingredient.adapter.IngredientItemAdapter
import com.nuda.nudaclient.presentation.product.ProductDetailActivity
import com.nuda.nudaclient.presentation.product.adapter.ProductAdapter
import com.nuda.nudaclient.presentation.search.adapter.SearchIngredientAdapter
import retrofit2.http.Query

// intent에 검색 텍스트, 화면 모드 함께 전달 필요!!
class SearchResultActivity : BaseActivity() {
    // 검색 결과 화면으로 이동할 때 intent에 검색 상태 변수 전달 필요!!
    
    // TODO 상품 검색, 성분 검색 API 호출 및 응답 UI 업데이트

    private lateinit var binding: ActivitySearchResultBinding

    // 검색 결과 리사이클러뷰 어댑터
    private lateinit var productAdapter: ProductAdapter
    private lateinit var ingredientAdapter: SearchIngredientAdapter

    private lateinit var pageMode: String
    private lateinit var query: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pageMode = intent.getStringExtra("PAGEMODE") ?: ""
        query = intent.getStringExtra("query") ?: ""

        setToolbar() // 툴바 설정

        createAdapter() // 어댑터 생성
        switchMode(pageMode) // 화면 모드에 따른 어댑터
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setCartButton() // 장바구니 버튼만 추가
        setToolbarShadow(false) // 툴바 그림자 제거
    }

    // 어댑터 생성
    private fun createAdapter() {
        productAdapter = ProductAdapter() { productId ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }

        ingredientAdapter = SearchIngredientAdapter() { ingredientId -> // 성분 아이템 클릭 이벤트 설정
            // 성분 상세페이지로 이동, ingredientId 전달
            val intent = Intent(this, IngredientDetailActivity::class.java)
            intent.putExtra("INGREDIENT_ID", ingredientId)
            startActivity(intent)
        }
    }

    // 화면 모드에 따른 어댑터 선택
    private fun switchMode(pageMode: String) {
        when(pageMode) {
            "PRODUCT" -> {
                binding.rvSearchResult.adapter = productAdapter
            }
            "INGREDIENT" -> {
                binding.rvSearchResult.adapter = ingredientAdapter
            }
            else -> {
                Log.d("API_DEBUG", "검색 결과 화면 모드 오류")
            }
        }
    }
}