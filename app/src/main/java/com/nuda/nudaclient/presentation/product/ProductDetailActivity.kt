package com.nuda.nudaclient.presentation.product

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.RetrofitClient.reviewsService
import com.nuda.nudaclient.databinding.ActivityProductDetailBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.product.adapter.ProductImagesAdapter

class ProductDetailActivity : BaseActivity() {

    // TODO 상품 정보 조회 API 연동 및 데이터 바인딩
    // TODO 성분 성분 구성 요약 API 연동 및 데이터 바인딩
    // TODO 리뷰 요약 조회 API 연동 및 데이터 바인딩
    // TODO 리뷰 좋아요 API 연동 및 기능 구현

    private lateinit var binding: ActivityProductDetailBinding

    private lateinit var viewPagerProductImages: ViewPager2
    private lateinit var layoutIndicator: LinearLayout
    private lateinit var imageAdapter: ProductImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ViewPager 객체 초기화
        viewPagerProductImages = binding.viewPagerProductImages

        // 인디케이터 초기화
        layoutIndicator = binding.llIndicator

        // 툴바 설정
        setToolbar()

        // 상품 상세페이지 정보 로드
        loadProductDetail()

        // 버튼 설정
        setButtons()

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        binding.toolBar.toolbarShadow.visibility = View.GONE // 그림자 뷰 숨기기

        setToolbarButtons() // 툴바 버튼들 설정
    }

    // 상품 상세페이지 정보 로드
    private fun loadProductDetail() {
        loadProductInfo()
    }

    // 상품 정보 로드
    private fun loadProductInfo() {
        productsService.getProductInfo(1) // 묵업 데이터 입력 (productId = 1)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) { // data non-null로 보장
                        body.data?.let { data ->
                            // 뷰에 데이터 바인딩
                            binding.tvBrand.text = data.brandName
                            binding.tvProductName.text = data.name
                            binding.tvRatingScore.text = data.averageRating.toString()
                            binding.tvReview.text = "리뷰 ${data.reviewCount}개"
                            binding.tvPrice.text = data.price.toFormattedPrice()

                            // 리뷰 개수 텍스트에 밑줄 플래그 추가
                            binding.tvReview.paintFlags = binding.tvReview.paintFlags or Paint.UNDERLINE_TEXT_FLAG

                            // 상품 이미지 리스트
                            setupViewPager(data.imageUrls)
                            setupIndicator(data.imageUrls.size)

//                            // 상품 정보 이미지 (크롤링 후 수정)
//                            data.content

                            // 찜하기
                            if (data.likedByMe) { // 상품 찜하기 여부
                                binding.ivProductLike.setBackgroundResource(R.drawable.img_btn_heart_selected)
                            }

//                            if (data.) { // 브랜드 찜하기 여부
//                                binding.ivBrandLike.setBackgroundResource(R.drawable.img_heart2_selected)
//                            }
                        }
                    }
                }
            )
    }

    // 상품 이미지 리스트 ViewPager 설정
    private fun setupViewPager(imageUrls: List<String>) {
        // 어댑터 설정
        imageAdapter = ProductImagesAdapter(imageUrls)
        viewPagerProductImages.adapter = imageAdapter

        viewPagerProductImages.registerOnPageChangeCallback( // 이미지 스와이프할 때마다 알림
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateIndicator(position)
                }
            })
    }

    // 상품 이미지 리스트 인디케이터 점 생성
    private fun setupIndicator(imageCount: Int) {
        repeat(imageCount) { // 이미지 개수만큼 반복
            val indicator = View(this).apply { // View 객체 생성 및 설정
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
                setBackgroundResource(R.drawable.indicator_unselected)
            }
            layoutIndicator.addView(indicator) // 인디케이터 추가
        }

        if (layoutIndicator.childCount > 0) { // 인디케이터가 하나 이상일 때)
            layoutIndicator.getChildAt(0)
                .setBackgroundResource(R.drawable.indicator_selected) // 첫 번째 인디케이터 선택됨으로 변경
        }
    }

    // 상품 이미지 스와이프 시, 인디케이터 업데이트
    private fun updateIndicator(position: Int) {
        // 전체 인디케이터 unselected 설정
        for (i in 0 until layoutIndicator.childCount) { // 인디케이터 개수만큼 반복
            layoutIndicator.getChildAt(i)
                .setBackgroundResource(R.drawable.indicator_unselected)
        }

        // 선택된 이미지 인디테이터 selected 설정
        if (position < layoutIndicator.childCount) { // 인디케이터 개수보다 작을 때
            layoutIndicator.getChildAt(position)
                .setBackgroundResource(R.drawable.indicator_selected)
        }
    }

    private fun setButtons() {
        setMoveToReviewPage()
    }

    // 전체 리뷰 화면으로 이동
    private fun setMoveToReviewPage() {
        binding.llMoveToReviewPage.setOnClickListener {
            // 전체 리뷰 화면으로 이동
//            startActivity(Intent(this, ReviewActivity::class.java))
        }
    }

    // 리뷰 카드의 좋아요 클릭 시 (좋아요 하거나 취소) -> 어댑터에 추가?? linearLayout도 어댑터 적용 되나. 이거 전체 리뷰에도 같이 써야할 것 같은디
    private fun setLikeReview(reviewId: Int) {
        reviewsService.likeReview(reviewId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            if (data.liked) { // true : 좋아요
                                // 리뷰 아이템 카드의 좋아요 하트 변경 (selected로)
                            } else { // false : 취소
                                // 리뷰 아이템 카드의 좋아요 하트 변경 (unselected로)
                            }
                        }
                    }
                }
            )

    }




}