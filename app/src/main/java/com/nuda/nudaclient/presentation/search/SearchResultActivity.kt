package com.nuda.nudaclient.presentation.search

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivitySearchResultBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class SearchResultActivity : BaseActivity() {
    // 검색 결과 화면으로 이동할 때 intent에 검색 상태 변수 전달 필요!!

    // TODO 제품 검색 결과, 상품 검색 결과 함께 사용. intent에 검색 상태 변수 전달받아서 화면 로드하기
    // TODO 제품, 성분 검색 분기에 따라 각각 다른 어댑터 연결 필요. 어댑터 설정 각각 하기
    // TODO 제품 카드 클릭 시 제품 상세페이지 이동 처리 (intent 전달 필요)
    // TODO 성분 카드 클릭 시 성분 상세페이지 이동 처리  (intent 전달 필요)

    private lateinit var binding : ActivitySearchResultBinding

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