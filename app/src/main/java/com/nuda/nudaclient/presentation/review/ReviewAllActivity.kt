package com.nuda.nudaclient.presentation.review

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.animation.Positioning
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.RetrofitClient.reviewsService
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetRankingByKeywordResponse
import com.nuda.nudaclient.databinding.ActivityReviewAllBinding
import com.nuda.nudaclient.databinding.ItemReviewKeywordsBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.setInfiniteScrollListener
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.product.ProductDetailActivity
import com.nuda.nudaclient.presentation.product.adapter.ProductAdapter
import com.nuda.nudaclient.presentation.review.adapter.ReviewAdapter
import com.nuda.nudaclient.utils.CustomToast

class ReviewAllActivity : BaseActivity() {
    
    // TODO 별점, 리뷰 수 바인딩 필요

    // TODO 전체 리뷰 목록 리사이클러뷰 설정 및 어댑터 연결
    // TODO 키워드 필터링 설정 및 목록 로드
    // TODO 좋아요 버튼 클릭 로직 구현

    // TODO 리뷰 작성 후 리뷰 목록에 리뷰 추가되도록

    private lateinit var binding: ActivityReviewAllBinding
    private var productId = -1

    private lateinit var reviewAdapter: ReviewAdapter
    private var selectedSortType: String = "DEFAULT" // 선택된 필터링 저장

    private var currentCursor: Int? = null // 다음 페이지 요청에 쓸 커서
    private var isLoading = false // 현재 로딩 중인지 체크

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityReviewAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Intent에 담긴 productId 저장
        productId = intent.getIntExtra("PRODUCT_ID", -1)

        setToolbar()
        setupRecyclerView() // 리사이클러뷰 설정

        setCreateReviewBtn() // 리뷰 쓰기 버튼 설정
        setKeywordFilter() // 키워드 필터링 설정

        loadProductInfo() // 별점, 리뷰 수 로드
        loadReviewKeyword() // 긍정/부정 키워드 로드
    }

    // 리뷰 작성 후 돌아올 때 목록 및 개수 갱신
    override fun onResume() {
        super.onResume()

        loadProductInfo() // 별점, 리뷰 수
        loadReviewKeyword() // 긍정, 부정 키워드
        loadReviews() // 리뷰 목록
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("전체 리뷰") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarShadow(false) // 그림자 숨김
    }

    // 리뷰 쓰기 버튼 클릭 설정
    private fun setCreateReviewBtn() {
        binding.btnWriteReview.setOnClickListener {
            val intent = Intent(this, ReviewCreateActivity::class.java)
            intent.putExtra("STATE", "product")
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
    }

    // 별점, 리뷰 수 로드
    private fun loadProductInfo() {
        productsService.getProductInfo(productId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            binding.tvStar.text = data.averageRating.toString()
                            binding.tvReviewCount.text = "(${data.reviewCount})"

                            Log.d("API_DEBUG", "평점: ${data.averageRating}, 리뷰수: ${data.reviewCount}")
                        }
                    }
                })
    }

    // 긍정/부정 키워드 로드
    private fun loadReviewKeyword() {
        reviewsService.getReviewKeywords(productId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            // 긍정 키워드
                            if (data.positive.isEmpty()) { // 긍정 키워드가 없는 경우
                                binding.llPositiveItems.visibility = View.GONE
                                binding.tvNoPositiveKeyword.visibility = View.VISIBLE
                                Log.d("API_DEBUG", "긍정키워드가 없습니다")
                            } else { // 긍정 키워드가 있는 경우
                                binding.llPositiveItems.visibility = View.VISIBLE
                                binding.tvNoPositiveKeyword.visibility = View.GONE
                                addKeywordItems(binding.llPositiveItems, data.positive)
                                Log.d("API_DEBUG", "positive: ${data.positive}")
                            }
                            // 부정 키워드
                            if (data.negative.isEmpty()) { // 부정 키워드가 없는 경우
                                binding.llNegativeItems.visibility = View.GONE
                                binding.tvNoNegativeKeyword.visibility = View.VISIBLE
                                Log.d("API_DEBUG", "긍정키워드가 없습니다")
                            } else { // 부정 키워드가 있는 경우
                                binding.llNegativeItems.visibility = View.VISIBLE
                                binding.tvNoNegativeKeyword.visibility = View.GONE
                                addKeywordItems(binding.llNegativeItems, data.negative)
                                Log.d("API_DEBUG", "negative: ${data.negative}")
                            }
                        }
                    }
                },
                onError = { errorResponse ->
                    when (errorResponse?.code) {
                        "ML_REVIEW_INSUFFICIENT" -> { // 리뷰 개수가 10개 미만일 경우
                            binding.llPositiveItems.visibility = View.GONE
                            binding.tvNoPositiveKeyword.visibility = View.VISIBLE
                            binding.llNegativeItems.visibility = View.GONE
                            binding.tvNoNegativeKeyword.visibility = View.VISIBLE
                            Log.d("API_DEBUG", "리뷰 개수가 10개 미만입니다")
                        }
                        "PRODUCT_INVALID" -> { // 존재하지 않는 상품일 경우
                            binding.llPositiveItems.visibility = View.GONE
                            binding.tvNoPositiveKeyword.visibility = View.VISIBLE
                            binding.llNegativeItems.visibility = View.GONE
                            binding.tvNoNegativeKeyword.visibility = View.VISIBLE
                            Log.d("API_DEBUG", "존재하지 않는 상품입니다")
                        }
                    }
                }
            )
    }

    // 긍정/부정 키워드 추가
    private fun addKeywordItems(container: LinearLayout, keywords: List<String>) {
        container.removeAllViews() // 기존 뷰 제거

        keywords.forEachIndexed { index, keyword ->
            val itemBinding = ItemReviewKeywordsBinding.inflate(
                layoutInflater,
                container,
                false
            )
            // 키워드 설정
            itemBinding.tvRank.text = "${index+1}"
            itemBinding.tvKeyword.text = keyword

            // 키워드 컨테이너에 추가
            container.addView(itemBinding.root)
        }
    }

    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter() { reviewId, position ->
            setLikeReview(reviewId, position) // 좋아요 버튼 클릭 시 동작 설정
        }
        binding.rvAllReviews.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(this@ReviewAllActivity)
        }

        loadReviews() // 첫 로드
        setScrollListner()  // 무한 스크롤 설정
    }

    // 무힌 스크롤 리스너 설정
    private fun setScrollListner() {
        binding.rvAllReviews.setInfiniteScrollListener {
            if (!isLoading // 로딩 중이 아니고
                && currentCursor != null) { // 다음 페이지가 있으면
                loadReviews() // 다음 페이지 로드
            }
        }
    }

    // 리뷰 좋아요 버튼 설정
    private fun setLikeReview(reviewId: Int, position: Int) {
        reviewsService.likeReview(reviewId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            // 어댑터의 좋아요 업데이트 메소드
                            reviewAdapter.updateLikeState(
                                position = position,
                                likedByMe = data.liked,
                                likeCount = data.likeCount)
                        }
                    }
                }
            )
    }

    // 리뷰 목록 로드
    private fun loadReviews() {
        reviewsService.getReviewRankingByKeyword(
            productId = productId, // 상품 아이디
            keyword = selectedSortType, // 선택된 키워드
            cursor = currentCursor // 다음 페이지 cursor
        ).executeWithHandler(
            context = this,
            onSuccess = { body ->
                if (body.success == true) {
                    body.data?.let { data ->
                        if (currentCursor == null) { // 첫 로드이거나 필터 변경 후 첫 로드인 경우
                            reviewAdapter.submitList(data.content) // 아이템 초기화

                            if (data.content.isEmpty()) { // 값이 비었을 때
                                binding.tvNoReview.visibility = View.VISIBLE
                                binding.rvAllReviews.visibility = View.GONE
                            } else { // 값이 있을 때
                                binding.tvNoReview.visibility = View.GONE
                                binding.rvAllReviews.visibility = View.VISIBLE
                            }
                        } else { // 첫 로드가 아닌 경우
                            reviewAdapter.appendItems(data.content) // 무한 스크롤 추가
                        }

                        // 다음 커서 업데이트
                        currentCursor = if (data.hasNext) { // 다음 페이지가 있으면
                            data.nextCursor
                        } else { // 마지막 페이지면
                            null
                        }
                    }
                }
            }
        )
    }

    // 키워드 필터링 버튼 클릭 설정
    private fun setKeywordFilter() {
        binding.tvFiliterAll.isSelected = true // 전체 기본값 디폴트 설정
        
        binding.llFiliterKeyword.children.forEach { keyword ->
            // 키워드 필터 클릭 동작
            keyword.setOnClickListener {
                // 전체 키워드 선택 해제
                binding.llFiliterKeyword.children.forEach { it.isSelected = false }
                // 클릭한 것만 선택
                keyword.isSelected = true
                // 선택된 키워드를 태그로 문자열 매칭
                selectedSortType = keyword.tag as String
                // cursor 초기화
                currentCursor = null

                loadReviews() // 리뷰 목록 로드
            }

        }
    }
}