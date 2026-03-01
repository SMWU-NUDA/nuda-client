package com.nuda.nudaclient.presentation.search

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivitySearchBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class SearchActivity : BaseActivity() {

    // TODO 제품, 상품 검색 버튼 클릭에 따라 UI 업데이트 - 화면 상태 변수 필요!
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
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setCartButton() // 장바구니 버튼만 추가
        setToolbarShadow(false) // 툴바 그림자 제거
    }

}