package com.nuda.nudaclient.presentation.review

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivityReviewCreateBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class ReviewCreateActivity : BaseActivity() {

    // TODO 아이템 카드의 순위 삭제
    // TODO 필수 사항 유효성 검사
    // TODO 액티비티 중복 사용 설정 (하단 참고)

    /**
     * [ 액티비티 중복 사용 설정 ]
     * 마이페이지-내 리뷰 쓰기 / 상품 상세페이지-새 리뷰 쓰기 중복
     * 마이페이지일 경우와 상품 상세페이지일 경우 intent에 플래그나 상태 변수 담아서 전달(state = product / mypage 등)
     * 각 플래그에 따라 검색바 설정 및 리뷰 영역 작성 불가 처리
     */

    private lateinit var binding: ActivityReviewCreateBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityReviewCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 툴바 설정
        setToolbar()
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("새 리뷰 쓰기") // 타이틀
        setBackButton() // 뒤로가기 버튼
    }
}