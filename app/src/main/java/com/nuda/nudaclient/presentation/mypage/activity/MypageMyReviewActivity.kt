package com.nuda.nudaclient.presentation.mypage.activity

import android.os.Binder
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.reviewsService
import com.nuda.nudaclient.databinding.ActivityMypageMyreviewBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.mypage.adapter.MypageMyReviewAdapter

class MypageMyReviewActivity : BaseActivity() {

    // TODO: feat(reviews): (mypage) 내 리뷰 어댑터 생성 및 설정
    // TODO: feat(reviews): (mypage) 내 리뷰 리사이클러뷰 연결
    // TODO: feat(reviews): (mypage) 내 리뷰 기능 구현 및 API 호출

    private lateinit var binding: ActivityMypageMyreviewBinding

    private var currenCursor: Int? = null

    private lateinit var myreviewAdapter: MypageMyReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMypageMyreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 툴바 타이틀 변경
        setToolbarTitle("내 리뷰")
        // 툴바 뒤로가기 설정
        setBackButton()

        // 리사이클러뷰 설정
        setRecyclerView()

        // 리뷰 첫 페이지 로드
        loadReviews()

    }

    // 리사이클러뷰 설정
    private fun setRecyclerView() {
        // 1. 어댑터 설정
        myreviewAdapter = MypageMyReviewAdapter()
        binding.rvMyreview.adapter = myreviewAdapter
        // 2. 레이아웃 매니저 설정
        binding.rvMyreview.layoutManager = LinearLayoutManager(this)
    }

    // 리뷰 로드
    private fun loadReviews() {
        reviewsService.getMyReviews(cursor = currenCursor, size = 20)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {

                    }
                }

            )

    }

}