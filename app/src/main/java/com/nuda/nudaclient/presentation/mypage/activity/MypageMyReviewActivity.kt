package com.nuda.nudaclient.presentation.mypage.activity

import android.os.Binder
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.reviewsService
import com.nuda.nudaclient.databinding.ActivityMypageMyreviewBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.mypage.adapter.MypageMyReviewAdapter
import com.nuda.nudaclient.utils.CustomToast

class MypageMyReviewActivity : BaseActivity() {

    private lateinit var binding: ActivityMypageMyreviewBinding

    private var currenCursor: Int? = null // 다음 페이지 요청에 쓸 커서
    private var isLoading = false // 현재 로딩 중인지 체크

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
        myreviewAdapter = MypageMyReviewAdapter() // 빈 어댑터 생성 content = []
        // 2. 리사이클러뷰에 어댑터, 레이아웃 매니저 연결
        binding.rvMyreview.apply {
            adapter = myreviewAdapter
            layoutManager = LinearLayoutManager(this@MypageMyReviewActivity)
        }
        // 3. 스크롤 리스너 등록 (무한 스크롤 감지)
        setScrollListner()
        // 4. 리뷰 삭제 버튼 클릭 시 실행될 로직 설정
        myreviewAdapter.onDeleteClickListner = { reviewId, position ->
            deleteReview(reviewId, position)
        }
    }

    // 스크롤 리스너 설정
    private fun setScrollListner() {
        // addOnScrollListener: 사용자가 스크롤할 때마다 자동으로 알려줌
        binding.rvMyreview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // onScrolled(): 스크롤이 발생하면 자동 호출되는 함수
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 1. 레이아웃 메니저 가져오기
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                // 2. 현재 화면에 보이는 마지막 아이템 위치
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                // 3. 전체 아이템 개수
                val totalItemCount = layoutManager.itemCount

                // 4. 끝에 가까워졌는지 체크
                if (!isLoading && // 로딩 중이 아니고
                    currenCursor != null && // 다음 페이지가 있고
                    lastVisiblePosition >= totalItemCount - 3) { // 끝에서 3개 전이면

                    loadReviews() // 다음 페이지 로드
                }
            }
        })
    }

    // 리뷰 삭제 (API 호출)
    private fun deleteReview(reviewId: Int, position: Int) {
        reviewsService.deleteMyReview(reviewId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        // 리뷰 삭제 성공 시 어댑터에서 아이템 제거
                        myreviewAdapter.removeItem(position)
                        CustomToast.show(binding.root, "리뷰가 삭제되었습니다")
                        Log.d("API_DEBUG", "리뷰 삭제 성공")
                    } else {
                        Log.d("API_DEBUG", "리뷰 삭제 실패")
                    }
                }
            )
    }

    // 리뷰 로드
    private fun loadReviews() {
        if (isLoading) return // 로딩 중이라면 리턴. 중복 호출 방지
        // 로딩 시작
        isLoading = true

        reviewsService.getMyReviews(cursor = currenCursor, size = 20)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        val data = body.data
                        if (data != null) { // 리뷰 데이터가 null이 아닐 때
                            // 어댑터에 새 데이터 추가
                            myreviewAdapter.addItems(data.content)

                            // 다음 커서 업데이트
                            currenCursor = if (data.hasNext) { // 다음 페이지 있으면 커서 저장
                                data.nextCursor
                            } else { // 마지막 페이지면 null
                                null
                            }
                        }
                    }
                    // 로딩 종료
                    isLoading = false
                },
                onError = {
                    isLoading = false
                }
            )


    }

}