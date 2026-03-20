package com.nuda.nudaclient.presentation.search

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivitySearchBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class SearchActivity : BaseActivity() {

    // TODO 제품 검색일 떄, 상품 검색일 때 나눠서 처리 필요 (API 호출도 따로 관리)
    // TODO 제품 검색 결과, 상품 검색 결과 둘 다 같은 액티비티지만 intent에 상태 변수 담아서 함께 전달

    private lateinit var binding : ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setToolbar() // 툴바 설정

        setSearchMode() // 화면 모드 설정


    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setCartButton() // 장바구니 버튼만 추가
        setToolbarShadow(false) // 툴바 그림자 제거
    }

    // 화면 모드에 따른 UI 업데이트 (인기 검색어 API 호출)
    private fun setUIByMode(pageMode: String) {
        when(pageMode) {
            "product" -> {
                
            }
            "ingredient" -> {

            }
        }
    }


    // 모드 선택 버튼
    private fun setSearchMode() {
        // (기본값) 제품 검색
        setSearchModeButton(true)
        setUIByMode("product")

        // 제품 검색 버튼
        binding.btnSearchProduct.setOnClickListener {
            setSearchModeButton(true)
            setUIByMode("product")
        }
        // 상품 검색 버튼
        binding.btnSearchIngredient.setOnClickListener {
            setSearchModeButton(false)
            setUIByMode("ingredient")
        }
    }

    // 버튼 모드 설정
    private fun setSearchModeButton(isProductMode: Boolean) {
        binding.btnSearchProduct.isSelected = isProductMode
        binding.btnSearchIngredient.isSelected = !isProductMode
    }

    // 검색어 자동 완성
    // TODO 검색어 자동 완성
}