package com.nuda.nudaclient.presentation.review

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivityReviewCreateBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity

class ReviewCreateActivity : BaseActivity() {
    // 리뷰 작성 화면으로 이동할 때 Intent에 상태 변수 담아서 전달 필요 !!! (state : product / mypage)

    // TODO 필수 사항 유효성 검사 : (필수)별점, 상세 리뷰 (선택) 사진 첨부
    // TODO 별점 설정
    // TODO 사진 첨부 설정 (갤러리 열기, S3 Presigned URL API 호출)
    // TODO 리뷰 작성 API 호출

    /**
     * [ 액티비티 중복 사용 설정 ]
     * 마이페이지-내 리뷰 쓰기 / 상품 상세페이지-새 리뷰 쓰기 중복
     * 마이페이지일 경우와 상품 상세페이지일 경우 intent에 플래그나 상태 변수 담아서 전달(state = product / mypage 등)
     * 각 플래그에 따라 검색바 설정 및 리뷰 영역 작성 불가 처리
     */

    private lateinit var binding: ActivityReviewCreateBinding

    // 액티비티 중복 사용을 위한 상태 변수
    private lateinit var state: String // product, mypage

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

        // Intent에서 상태 변수 값 받기
        state = intent.getStringExtra("STATE") ?: ""


        // 툴바 설정
        setToolbar()

        // 상태 변수에 따른 화면 로드
        loadScreen()
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("새 리뷰 쓰기") // 타이틀
        setBackButton() // 뒤로가기 버튼
    }

    // 상태 변수에 따른 화면 로드
    private fun loadScreen() {
        binding.itemProductCard.tvRank.visibility = View.GONE // 상품 아이템 카드의 순위 제거

        // state = product : 상품 상세페이지의 리뷰 작성일 때 - 상품 정해짐
        if (state == "mypage") { // 마이페이지의 리뷰 작성일 때 - 상품 선택 필요
            binding.clSearch.visibility = View.VISIBLE // 검색바 설정
            binding.itemProductCard.root.visibility = View.GONE // 상품 아이템 카드 설정
            binding.viewOverlay.visibility = View.VISIBLE // 오버레이 설정
        }
    }

}