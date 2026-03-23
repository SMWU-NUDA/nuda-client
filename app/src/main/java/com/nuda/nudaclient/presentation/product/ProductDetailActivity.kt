package com.nuda.nudaclient.presentation.product

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.ingredientsService
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.RetrofitClient.reviewsService
import com.nuda.nudaclient.data.remote.RetrofitClient.shoppingService
import com.nuda.nudaclient.data.remote.api.IngredientsService
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetSummaryResponse
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingCreateOrderRequest
import com.nuda.nudaclient.databinding.ActivityProductDetailBinding
import com.nuda.nudaclient.databinding.ItemReviewKeywordsBinding
import com.nuda.nudaclient.databinding.ItemTrendBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.ingredient.IngredientComponentActivity
import com.nuda.nudaclient.presentation.product.adapter.ProductImagesAdapter
import com.nuda.nudaclient.presentation.review.ReviewAllActivity
import com.nuda.nudaclient.presentation.review.ReviewCreateActivity
import com.nuda.nudaclient.presentation.review.adapter.ReviewAdapter
import com.nuda.nudaclient.presentation.shopping.ShoppingCartActivity
import com.nuda.nudaclient.presentation.shopping.ShoppingOrderCompleteActivity
import com.nuda.nudaclient.utils.CustomToast
import com.nuda.nudaclient.utils.setupBarGraph

class ProductDetailActivity : BaseActivity() {
    // 상품 상세페이지로 이동할 때 Intent에 productId 담아서 전달 필요 !!!

    private lateinit var binding: ActivityProductDetailBinding

    private lateinit var viewPagerProductImages: ViewPager2
    private lateinit var layoutIndicator: LinearLayout
    private lateinit var imageAdapter: ProductImagesAdapter

    private lateinit var reviewAdapter: ReviewAdapter

    // 탭 레이아웃 관련 변수
    private var productInfoSectionTop = 0 // 상품 정보 섹션 상단 위치 저장
    private var ingredientSectionTop = 0 // 성분 섹션 상단 위치 저장
    private var reviewSectionTop = 0 // 리뷰 섹션 상단 위치 저장
    private var isUserScrolling = true // 프로그래밍 스크롤 시 탭 변경 방지용
    private var stickyTabHeight = 0 // 고정 탭 높이

    private var productId: Int = -1
    private var brandId: Int = -1

    // 성분 화면에서 돌아올 때 결과를 받는 런처
    private val ingredientComponentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadIngredientInfo() // 관심,피하기 개수 텍스트 갱신
        }
    }

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

        // Intent에서 productId 받기
        productId = intent.getIntExtra("PRODUCT_ID", -1)
        Log.d("API_DEBUG", "productId: $productId")

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

        // 탭 레이아웃 설정
        setupTabLayout()
        measureSectionPositions()
        setupScrollListener()

    }

    override fun onResume() {
        super.onResume()

        loadProductInfo()
        loadIngredientInfo()
        loadReviewList()
        loadReviewSummary()
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarButtons() // 툴바 버튼들 설정
    }

    // TabLayout 초기 설정
    private fun setupTabLayout() {
        // 탭 클릭 시 스크롤 이동하는 리스너 (탭 원본, sticky 공통 동작)
        fun onTabClick(position: Int) {
            if (!isUserScrolling) return

            val targetY = when (position) {
                0 -> productInfoSectionTop - stickyTabHeight
                1 -> ingredientSectionTop - stickyTabHeight
                2 -> reviewSectionTop - stickyTabHeight
                else -> 0
            }
            isUserScrolling = false
            binding.nestedScrollView.smoothScrollTo(0, targetY)
        }

        // 원본 탭 리스너
        binding.tabOriginal.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { onTabClick(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // sticky 탭 리스너 (동일한 동작)
        binding.tabSticky.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { onTabClick(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // 섹션 위치 측정 및 저장
    private fun measureSectionPositions() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener { // 뷰 측적 및 배치된 후 시점 알려주는 리스너
            override fun onGlobalLayout() {
                // 리스너 즉시 제거 - onGlobalLayout은 레이아웃 변경마다 계속 호출되기 때문
                // 제거 안 하면 화면 회전, 키보드 등장 등 매 변경마다 불필요하게 재호출됨
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

                stickyTabHeight = binding.tabOriginal.height

                productInfoSectionTop = getRelativeTop(binding.constraintProductInfo, binding.nestedScrollView)
                ingredientSectionTop = getRelativeTop(binding.line3, binding.nestedScrollView)
                reviewSectionTop = getRelativeTop(binding.line4, binding.nestedScrollView)
            }
        })
    }

    // 섹션 위치 가져오는 함수
    private fun getRelativeTop(view: View, scrollView: NestedScrollView): Int {
        var offset = 0
        var current: View = view

        // 목표 뷰부터 시작해서 NestedScrollView까지 올라가며 top 누적
        while (current != scrollView) {
            offset += current.top  // 현재 뷰의 부모 기준 top
            current = current.parent as View  // 한 단계 위 부모로 이동
        }
        return offset
    }

    // 스크롤 연동
    private fun setupScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                // 이미지 비동기 로드로 높이가 바뀔 수 있으므로 스크롤마다 섹션 위치 재계산
                productInfoSectionTop = getRelativeTop(binding.constraintProductInfo, binding.nestedScrollView)
                ingredientSectionTop = getRelativeTop(binding.line3, binding.nestedScrollView)
                reviewSectionTop = getRelativeTop(binding.line4, binding.nestedScrollView)

                // scrollY: 현재 스크롤된 Y 픽셀값 (위에서 얼마나 내려왔는지)
                updateTabByScrollPosition(scrollY)

                // tab_original의 하단이 화면 밖으로 나갔으면 sticky 탭 표시
                // binding.tabOriginal.bottom: 뷰 자체 좌표 기준 하단 위치
                // scrollY가 그보다 크다 = 원본 탭이 화면 위로 사라졌다
                val tabOriginalTop = getRelativeTop(binding.tabOriginal, binding.nestedScrollView)
                if (scrollY > tabOriginalTop) {
                    binding.tabSticky.visibility = View.VISIBLE
                } else {
                    binding.tabSticky.visibility = View.GONE
                }

                // smoothScrollTo() 완료 후 플래그 복구
                // (스크롤 중 계속 호출되므로 스크롤 끝나면 자연스럽게 true로 돌아옴)
                if (!isUserScrolling) {
                    isUserScrolling = true
                }
            }
        )
    }

    // 스크롤 위치에 따라 탭 업데이트
    private fun updateTabByScrollPosition(scrollY: Int) {
        val tabIndex = when {
            scrollY < ingredientSectionTop - stickyTabHeight  -> 0
            scrollY < reviewSectionTop - stickyTabHeight -> 1
            else -> 2
        }

        // 이미 선택된 탭이면 아무것도 안 함 → select() 호출 자체를 막아서 리스너 발동 방지
        if (binding.tabOriginal.selectedTabPosition != tabIndex) {
            isUserScrolling = false // 코드가 탭을 바꾸는 것임을 표시 → 탭 리스너에서 스크롤 안 함
            binding.tabOriginal.getTabAt(tabIndex)?.select()
            binding.tabSticky.getTabAt(tabIndex)?.select()
            isUserScrolling = true  // 즉시 복구
        }
    }


    // 상품 상세페이지 정보 로드
    private fun loadProductDetail() {
        loadProductInfo()
        loadIngredientInfo()
        loadReviewInfo()
    }

    // 상품 정보 로드
    private fun loadProductInfo() {
        productsService.getProductInfo(productId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) { // data non-null로 보장
                        body.data?.let { data ->
                            // 브랜드 아이디 값 저장
                            brandId = data.brandId

                            // 뷰에 데이터 바인딩
                            binding.tvBrand.text = data.brandName
                            binding.tvProductName.text = data.name
                            binding.tvRatingScore.text = data.averageRating.toString()
                            binding.tvReview.text = "리뷰 ${data.reviewCount}개"
                            binding.tvPrice.text = data.price.toFormattedPrice()
                            binding.tvReviewRatingScoreAndCount.text = "${data.averageRating}(${data.reviewCount})"
                            binding.btnMoveToAllReviewsBottom.text = "${data.reviewCount}개 리뷰 전체보기"

                            // 리뷰 개수 텍스트에 밑줄 플래그 추가
                            binding.tvReview.paintFlags = binding.tvReview.paintFlags or Paint.UNDERLINE_TEXT_FLAG

                            // 상품 이미지 리스트
                            setupViewPager(data.mainImageUrls)
                            setupIndicator(data.mainImageUrls.size)

                            // 상품 정보 이미지 (크롤링 후 수정)
                            setupDetailImages(data.detailImageUrls)

                            // 찜하기
                            if (data.productLikedByMe) { // 상품 찜하기 여부
                                binding.ivProductLike.setImageResource(R.drawable.img_btn_heart_selected)
                            } else {
                                binding.ivProductLike.setImageResource(R.drawable.img_btn_heart_unselected)
                            }

                            if (data.brandLikedByMe) { // 브랜드 찜하기 여부
                                binding.ivBrandLike.setImageResource(R.drawable.img_heart2_selected)
                            } else {
                                binding.ivBrandLike.setImageResource(R.drawable.img_heart2_unselected)
                            }
                        }
                    }
                }
            )
    }

    // 상품 정보 이미지 동적 로드 함수
    private fun setupDetailImages(imageUrls: List<String>) {
        val container = binding.llDetailImages

        container.removeAllViews() // 중복 방지

        if (imageUrls.isEmpty()) {
            // 이미지 없으면 더보기 버튼과 그라데이션 숨김
            binding.btnProductInfo.visibility = View.GONE
            binding.viewGradientOverlay.visibility = View.GONE
            return
        }

        // URL 리스트 순서대로 ImageView 추가
        imageUrls.forEach { imageUrl ->
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                adjustViewBounds = true // 비율 유지
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
            container.addView(imageView)
        }

        // 초기 상태: 이미지 높이 400dp로 제한
        container.layoutParams = container.layoutParams.apply {
            height = (350 * resources.displayMetrics.density).toInt()
        }
        container.clipChildren = true // 넘치는 부분 잘라냄

        // 더보기 버튼 클릭 이벤트
        binding.btnProductInfo.setOnClickListener {
            container.layoutParams = container.layoutParams.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            binding.viewGradientOverlay.visibility = View.GONE // 그라데이션 숨김
            binding.btnProductInfo.visibility = View.GONE      // 버튼 숨김
        }
    }

    // 성분 정보 로드
    private fun loadIngredientInfo() {
        ingredientsService.getIngredientSummary(productId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        updateIngredientUI(body.data)
                    }
                }
            )
    }

    // 성분 정보 UI 업데이트
    private fun updateIngredientUI(data: IngredientsGetSummaryResponse?) {
        data?.let { data ->
            // 막대그래프 구성
            setupBarGraph( // 표지
                binding.llTopSheet,
                data.ingredientCounts.topSheet.count,
                data.ingredientCounts.topSheet.riskCounts)
            setupBarGraph( // 흡수체
                binding.llAbsorber,
                data.ingredientCounts.absorber.count,
                data.ingredientCounts.absorber.riskCounts)
            setupBarGraph( // 방수층
                binding.llBackSheet,
                data.ingredientCounts.backSheet.count,
                data.ingredientCounts.backSheet.riskCounts)
            setupBarGraph( // 접착제
                binding.llAdhesive,
                data.ingredientCounts.adhesive.count,
                data.ingredientCounts.adhesive.riskCounts)
            setupBarGraph( // 기타
                binding.llAdditive,
                data.ingredientCounts.additive.count,
                data.ingredientCounts.additive.riskCounts)

            // 구성요소 별 성분 개수 텍스트
            binding.tvTopSheetCount.text = "${data.ingredientCounts.topSheet.count}개"
            binding.tvAbsorberCount.text = "${data.ingredientCounts.absorber.count}개"
            binding.tvBackSheetCount.text = "${data.ingredientCounts.backSheet.count}개"
            binding.tvAdhesiveCount.text = "${data.ingredientCounts.adhesive.count}개"
            binding.tvAdditiveCount.text = "${data.ingredientCounts.additive.count}개"

            // 항목 별 개수 텍스트
            binding.tvIngredientsAllCount.text = "${data.totalCount}개"
            binding.tvIngredientsCautionCount.text = "${data.globalRiskCounts.warn}개"
            binding.tvIngredientsDangerCount.text = "${data.globalRiskCounts.danger}개"
            binding.tvIngredientsHighlightCount.text = "${data.myIngredientCounts.prefer}개"
            binding.tvIngredientsAvoidCount.text = "${data.myIngredientCounts.avoided}개"
        }
    }

    // 리뷰 정보 로드
    private fun loadReviewInfo() {
        setupRecyclerView()
        loadReviewSummary()
    }

    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter() { reviewId, position ->
            setLikeReview(reviewId, position) // 좋아요 버튼 클릭 시 동작 설정
        }
        binding.rvReviews.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(this@ProductDetailActivity)
        }

        loadReviewList() // 리뷰 목록 로드
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
    private fun loadReviewList() {
        reviewsService.getReviewRankingByKeyword(
            productId = productId, // 상품 아이디
            keyword = "DEFAULT", // 선택된 키워드
            cursor = null
        ).executeWithHandler(
            context = this,
            onSuccess = { body ->
                Log.d("API_DEBUG", "리뷰 목록 응답: ${body.success}")
                if (body.success == true) {
                    body.data?.let { data ->
                        Log.d("API_DEBUG", "(상품 상세페이지) 리뷰 개수: ${data.content.size}")

                        if (data.content.isEmpty()) { // 값이 비었을 때
                            binding.tvNoReview.visibility = View.VISIBLE
                            binding.rvReviews.visibility = View.GONE
                        } else { // 값이 있을 때
                            binding.tvNoReview.visibility = View.GONE
                            binding.rvReviews.visibility = View.VISIBLE

                            // 어댑터에 최대 5개 리뷰 전달
                            reviewAdapter.submitList(data.content.take(5))
                        }
                    }
                }
            }
        )
    }

    // 리뷰 AI 요약 조회
    private fun loadReviewSummary() {
        reviewsService.getReviewSummary(productId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        Log.d("API_DEBUG", "리뷰 AI 요약 조회 성공")
                        body.data?.let { data ->
                            // ml 서버가 죽었을 경우
                            if (data.keywords.positive.isEmpty()
                                && data.keywords.negative.isEmpty()
                                && data.satisfactionRate == 50
                                && data.trendHighlights.isEmpty()) {
                                binding.reviewSummaryGroup.visibility = View.GONE
                                binding.noReviewSummary.visibility = View.VISIBLE
                                binding.nonExistProduct.visibility = View.GONE

                                CustomToast.show(binding.root, "ml 서버가 응답하지 않습니다")
                                Log.d("API_DEBUG", "ml 서버가 응답하지 않습니다")
                                return@executeWithHandler
                            }

                            binding.reviewSummaryGroup.visibility = View.VISIBLE
                            binding.noReviewSummary.visibility = View.GONE
                            binding.nonExistProduct.visibility = View.GONE

                            // 긍정/부정 키워드 설정
                            addKeywordItems(binding.llPositiveItems, data.keywords.positive)
                            addKeywordItems(binding.llNegativeItems, data.keywords.negative)

                            Log.d("API_DEBUG", "positive: ${data.keywords.positive}")
                            Log.d("API_DEBUG", "negative: ${data.keywords.negative}")

                            // 만족도 설정
                            binding.tvUserSatisfying.text = "사용자 만족도 ${data.satisfactionRate}%"
                            binding.progressBar.setProgress(data.satisfactionRate)
                            // 100%면 drawable 교체
                            if (data.satisfactionRate == 100) {
                                binding.progressBar.progressDrawable =
                                    ContextCompat.getDrawable(this, R.drawable.progress_satisfaction_full)
                            } else {
                                binding.progressBar.progressDrawable =
                                    ContextCompat.getDrawable(this, R.drawable.progress_satisfaction)
                            }

                            // 트렌드 설정
                            addTrendItems(binding.llTrendsItems, data.trendHighlights)
                            Log.d("API_DEBUG", "trends: ${data.trendHighlights}")
                        }
                    }
                },
                onError = { errorResponse ->
                    when (errorResponse?.code) {
                        "ML_REVIEW_INSUFFICIENT" -> { // 리뷰 개수가 10개 미만일 경우
                            binding.reviewSummaryGroup.visibility = View.GONE
                            binding.noReviewSummary.visibility = View.VISIBLE
                            binding.nonExistProduct.visibility = View.GONE
                        }
                        "PRODUCT_INVALID" -> { // 존재하지 않는 상품일 경우
                            binding.reviewSummaryGroup.visibility = View.GONE
                            binding.noReviewSummary.visibility = View.GONE
                            binding.nonExistProduct.visibility = View.VISIBLE
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

    // 트렌드 추가
    private fun addTrendItems(container: LinearLayout, trends: List<String>) {
        container.removeAllViews() // 기존 뷰 제거

        trends.forEach { trend ->
            val itemBinding = ItemTrendBinding.inflate(
                layoutInflater,
                container,
                false
            )
            itemBinding.tvTrend.text = trend
            container.addView(itemBinding.root)
        }
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
        layoutIndicator.removeAllViews()

        // 8dp를 px로 변환 후 고정 크기 지정
        val dotSize = (8 * resources.displayMetrics.density).toInt()

        repeat(imageCount) { // 이미지 개수만큼 반복
            val indicator = View(this).apply { // View 객체 생성 및 설정
                layoutParams = LinearLayout.LayoutParams(
                    dotSize,
                    dotSize
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
        setBottomBarButtons()
        setMoveToIngredientPage()
        setMoveToReviewAllPage()
        setMoveToReviewCreatePage()
        setLikeButtons()
    }

    // 하단 바 버튼 설정 (장바구니, 바로 구매)
    private fun setBottomBarButtons() {
        // 장바구니 버튼 설정
        binding.btnCart.setOnClickListener {
            // 장바구니에 상품 추가 API 호출
            shoppingService.addToCart(productId)
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) {
                            // 장바구니 이동 팝업
                            AlertDialog.Builder(this)
                                .setTitle("장바구니에 담겼습니다")
                                .setPositiveButton("장바구니 이동") { _, _ ->
                                    startActivity(Intent(this, ShoppingCartActivity::class.java))
                                }
                                .setNegativeButton("계속 쇼핑하기", null) // 팝업 닫기
                                .show()

                            Log.d("API_DEBUG", "productId: ${body.data?.productId}, quantity: ${body.data?.quantity}")
                        }
                    }
                )
        }
        // 바로 구매 버튼 설정
        binding.btnOrder.setOnClickListener {
            // 장바구니에 상품 추가 API 호출
            shoppingService.addToCart(productId)
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) {
                            val items = listOf(
                                ShoppingCreateOrderRequest.Item(
                                    productId = productId,
                                    quantity = 1
                                )
                            )
                            // 1. 주문 등록 API 호출
                            shoppingService.createOrder(ShoppingCreateOrderRequest(items))
                                .executeWithHandler(
                                    context = this,
                                    onSuccess = { body ->
                                        if (body.success == true) {
                                            body.data?.let { data ->
                                                Log.d("API_DEBUG", "주문 1. 주문 등록 API 호출 성공")
                                                // 2. 결제 요청 API 호출
                                                shoppingService.createPayment(data.orderId)
                                                    .executeWithHandler(
                                                        context = this,
                                                        onSuccess = { body ->
                                                            if (body.success == true) {
                                                                body.data?.let { data ->
                                                                    Log.d("API_DEBUG", "주문 2. 결제 요청 API 호출 성공")
                                                                    // 결제 완료 화면으로 이동
                                                                    val intent = Intent(this,
                                                                        ShoppingOrderCompleteActivity::class.java)
                                                                    intent.putExtra("PAYMENT_ID", data.paymentId) // 결제 고유 식별자 전달
                                                                    startActivity(intent)
                                                                    finish()
                                                                }
                                                            }
                                                        }
                                                    )
                                            }
                                        }
                                    }
                                )
                        }
                    }
                )
        }
    }

    // 상품 구성 성분 화면으로 이동
    private fun setMoveToIngredientPage() {
        binding.subtitleAndButton.setOnClickListener {
            val intent = Intent(this, IngredientComponentActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            ingredientComponentLauncher.launch(intent)
        }
    }

    // 전체 리뷰 화면으로 이동
    private fun setMoveToReviewAllPage() {
        binding.llMoveToReviewPage.setOnClickListener {
            val intent = Intent(this, ReviewAllActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
        binding.btnMoveToAllReviewsClickArea.setOnClickListener {
            val intent = Intent(this, ReviewAllActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
        binding.btnMoveToAllReviewsBottom.setOnClickListener {
            val intent = Intent(this, ReviewAllActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
    }

    // 리뷰 작성 화면으로 이동
    private fun setMoveToReviewCreatePage() {
        binding.btnWriteReview.setOnClickListener {
            val intent = Intent(this, ReviewCreateActivity::class.java)
            intent.putExtra("STATE", "product")
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
    }

    // 찜하기 버튼 설정
    private fun setLikeButtons() {
        // 브랜드 찜하기 설정
        val btnBrandLike = binding.ivBrandLike
        btnBrandLike.setOnClickListener {
            productsService.createBrandLike(brandId) // brandId 임시 데이터. 이후 수정
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) {
                            body.data?.let { data ->
                                if (data.liked) { // true : 좋아요
                                    btnBrandLike.setImageResource(R.drawable.img_heart2_selected)
                                } else { // false : 취소
                                    btnBrandLike.setImageResource(R.drawable.img_heart2_unselected)
                                }
                            }

                        }
                    }
                )
        }
        // 상품 찜하기 설정
        val btnProductLike = binding.ivProductLike
        btnProductLike.setOnClickListener {
            productsService.createProductLike(productId) // brandId 임시 데이터. 이후 수정
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) {
                            body.data?.let { data ->
                                if (data.liked) { // true : 좋아요
                                    btnProductLike.setImageResource(R.drawable.img_btn_heart_selected)
                                } else { // false : 취소
                                    btnProductLike.setImageResource(R.drawable.img_btn_heart_unselected)
                                }
                            }

                        }
                    }
                )
        }
    }



}